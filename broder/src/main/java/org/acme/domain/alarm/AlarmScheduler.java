package org.acme.domain.alarm;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class AlarmScheduler {

    private static final Logger LOG = Logger.getLogger(AlarmScheduler.class);
    private static final Duration EVALUATION_TIMEOUT = Duration.ofSeconds(10);

    private final AlarmService alarmService;
    private final AlarmEvaluator alarmEvaluator;
    private final AlarmEvaluationOrchestrator orchestrator;

    private final Counter alarmsEvaluatedCounter;
    private final AtomicInteger alarmsFiringGauge;
    private final Timer schedulerDurationTimer;

    public AlarmScheduler(AlarmService alarmService,
                          AlarmEvaluator alarmEvaluator,
                          AlarmEvaluationOrchestrator orchestrator,
                          MeterRegistry meterRegistry) {
        this.alarmService = alarmService;
        this.alarmEvaluator = alarmEvaluator;
        this.orchestrator = orchestrator;

        this.alarmsEvaluatedCounter = Counter.builder("broder.alarms.evaluated")
                .description("Total number of alarm evaluations")
                .register(meterRegistry);

        this.alarmsFiringGauge = new AtomicInteger(0);
        meterRegistry.gauge("broder.alarms.firing", alarmsFiringGauge);

        this.schedulerDurationTimer = Timer.builder("broder.scheduler.duration")
                .description("Time taken for each scheduler cycle in seconds")
                .register(meterRegistry);
    }

    @Scheduled(every = "20s")
    @Blocking
    void run() {
        long cycleStart = System.currentTimeMillis();
        LOG.info("[SCHEDULER] Starting alarm evaluation cycle");

        List<Alarm> activeAlarms;
        try {
            activeAlarms = alarmService.listEnabledAlarms();
        } catch (Exception e) {
            LOG.errorf(e, "[SCHEDULER] Failed to fetch enabled alarms");
            return;
        }

        if (activeAlarms.isEmpty()) {
            LOG.info("[SCHEDULER] No active alarms to evaluate");
            alarmsFiringGauge.set(0);
            return;
        }

        LOG.infof("[SCHEDULER] Evaluating %d active alarm(s)", activeAlarms.size());

        int triggeredCount = 0;
        int resolvedCount = 0;
        int errorCount = 0;
        int firingCount = 0;

        for (Alarm alarm : activeAlarms) {
            try {
                LOG.debugf("[SCHEDULER] Evaluating alarm: id=%d, name='%s', query='%s'",
                        alarm.getId(), alarm.getName(), alarm.getCondition().query());

                double currentValue = alarmEvaluator.evaluate(alarm)
                        .await().atMost(EVALUATION_TIMEOUT);

                AlarmStatus previousStatus = alarm.getStatus();
                AlarmStatus newStatus = alarm.evaluate(currentValue);

                LOG.debugf("[SCHEDULER] Alarm id=%d evaluated: previous=%s, new=%s, value=%.6f",
                        alarm.getId(), previousStatus, newStatus, currentValue);

                orchestrator.process(alarm, currentValue, newStatus, LocalDateTime.now());
                alarmsEvaluatedCounter.increment();

                if (newStatus == AlarmStatus.FIRING) {
                    firingCount++;
                }

                if (newStatus == AlarmStatus.FIRING && previousStatus != AlarmStatus.FIRING) {
                    triggeredCount++;
                } else if (previousStatus == AlarmStatus.FIRING && newStatus == AlarmStatus.RESOLVED) {
                    resolvedCount++;
                }

            } catch (Exception e) {
                errorCount++;
                LOG.errorf(e, "[SCHEDULER] Unexpected error evaluating alarm id=%d, name='%s'",
                        alarm.getId(), alarm.getName());
                try {
                    orchestrator.processError(alarm, e, LocalDateTime.now());
                } catch (Exception nested) {
                    LOG.errorf(nested, "[SCHEDULER] Failed to process error state for alarm id=%d", alarm.getId());
                }
            }
        }

        alarmsFiringGauge.set(firingCount);

        long durationMs = System.currentTimeMillis() - cycleStart;
        schedulerDurationTimer.record(Duration.ofMillis(durationMs));

        LOG.infof("[SCHEDULER] Cycle completed in %d ms | alarms=%d, triggered=%d, resolved=%d, errors=%d, firing=%d",
                durationMs, activeAlarms.size(), triggeredCount, resolvedCount, errorCount, firingCount);
    }
}
