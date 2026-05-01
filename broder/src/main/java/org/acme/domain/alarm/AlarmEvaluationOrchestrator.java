package org.acme.domain.alarm;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.acme.domain.alarm.enums.AlarmStatus;
import org.acme.domain.history.History;
import org.acme.domain.history.HistoryService;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;

@ApplicationScoped
public class AlarmEvaluationOrchestrator {

    private static final Logger LOG = Logger.getLogger(AlarmEvaluationOrchestrator.class);

    private final AlarmService alarmService;
    private final HistoryService historyService;
    private final AlarmNotificationService notificationService;

    public AlarmEvaluationOrchestrator(AlarmService alarmService,
                                       HistoryService historyService,
                                       AlarmNotificationService notificationService) {
        this.alarmService = alarmService;
        this.historyService = historyService;
        this.notificationService = notificationService;
    }

    @Transactional
    public void process(Alarm alarm, double currentValue, AlarmStatus newStatus, LocalDateTime evaluatedAt) {
        boolean wasFiring = alarm.wasFiring();

        alarmService.recordEvaluation(alarm.getId(), newStatus, evaluatedAt);

        try {
            historyService.save(History.recordFor(alarm, newStatus, currentValue));
            LOG.tracef("[ORCHESTRATOR] History recorded for alarm id=%d", alarm.getId());
        } catch (Exception e) {
            LOG.warnf(e, "[ORCHESTRATOR] Failed to record history for alarm id=%d", alarm.getId());
        }

        if (newStatus == AlarmStatus.FIRING && !wasFiring) {
            notificationService.notifyFiring(alarm, currentValue);
        }

        if (wasFiring && newStatus == AlarmStatus.RESOLVED) {
            LOG.debugf("[ORCHESTRATOR] Alarm id=%d transitioned from FIRING to RESOLVED", alarm.getId());
            notificationService.notifyResolved(alarm, currentValue);
        }
    }

    @Transactional
    public void processError(Alarm alarm, Throwable error, LocalDateTime evaluatedAt) {
        LOG.warnf("[ORCHESTRATOR] Processing error for alarm id=%d: %s", alarm.getId(), error.getMessage());

        if (alarm.wasFiring()) {
            LOG.debugf("[ORCHESTRATOR] Alarm id=%d was FIRING before entering ERROR state", alarm.getId());
        }
        alarmService.recordEvaluation(alarm.getId(), AlarmStatus.ERROR, evaluatedAt);

        try {
            historyService.save(History.recordFor(alarm, AlarmStatus.ERROR, null));
        } catch (Exception e) {
            LOG.warnf(e, "[ORCHESTRATOR] Failed to record history for error state alarm id=%d", alarm.getId());
        }

        notificationService.notifyError(alarm, error);
    }
}
