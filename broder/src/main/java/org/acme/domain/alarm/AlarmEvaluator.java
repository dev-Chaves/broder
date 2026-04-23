package org.acme.domain.alarm;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.prometheus.PrometheusService;
import org.acme.domain.prometheus.PrometheusValueExtractor;
import org.acme.domain.prometheus.dto.PrometheusQueryRequestDTO;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AlarmEvaluator {

    private static final Logger LOG = Logger.getLogger(AlarmEvaluator.class);

    private final PrometheusService prometheusService;

    public AlarmEvaluator(PrometheusService prometheusService) {
        this.prometheusService = prometheusService;
    }

    public Uni<Double> evaluate(Alarm alarm) {
        LOG.debugf("[EVALUATOR] Starting evaluation for alarm id=%d, name='%s'", alarm.getId(), alarm.getName());
        LOG.debugf("[EVALUATOR] Condition: %s", alarm.getCondition());

        var request = new PrometheusQueryRequestDTO(alarm.getCondition().query(), null, null, null, null);

        return prometheusService.instantQuery(request)
                .map(response -> {
                    LOG.debugf("[EVALUATOR] Prometheus response received for alarm id=%d: status=%s, resultType=%s, resultCount=%d",
                            alarm.getId(),
                            response.status(),
                            response.data() != null ? response.data().resultType() : "null",
                            response.data() != null && response.data().result() != null ? response.data().result().size() : 0);

                    double currentValue = PrometheusValueExtractor.extract(response);

                    LOG.debugf("[EVALUATOR] Alarm id=%s extracted value: %.6f", (Object) alarm.getId(), currentValue);

                    return currentValue;
                })
                .onFailure().invoke(e -> LOG.errorf(e, "[EVALUATOR] Prometheus query failed for alarm id=%d, name='%s'", alarm.getId(), alarm.getName()));
    }
}
