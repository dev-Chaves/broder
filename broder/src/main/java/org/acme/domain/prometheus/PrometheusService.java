package org.acme.domain.prometheus;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.domain.prometheus.client.PrometheusClient;
import org.acme.domain.prometheus.dto.PrometheusLabelsResponseDTO;
import org.acme.domain.prometheus.dto.PrometheusQueryRequestDTO;
import org.acme.domain.prometheus.dto.PrometheusQueryResponseDTO;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

@ApplicationScoped
public class PrometheusService {

    private static final Logger LOG = Logger.getLogger(PrometheusService.class);

    @Inject
    @RestClient
    PrometheusClient prometheusClient;

    public Uni<PrometheusQueryResponseDTO> instantQuery(PrometheusQueryRequestDTO dto) {
        LOG.debugf("[PROMETHEUS] Instant query: %s", dto.query());
        return prometheusClient.instantQuery(dto.query(), dto.time())
                .onItem().invoke(response -> LOG.debugf(
                        "[PROMETHEUS] Instant query response: status=%s, resultType=%s, resultCount=%d",
                        response.status(),
                        response.data() != null ? response.data().resultType() : "null",
                        response.data() != null && response.data().result() != null ? response.data().result().size() : 0))
                .onFailure().invoke(e -> LOG.errorf(e, "[PROMETHEUS] Instant query failed: %s", dto.query()));
    }

    public Uni<PrometheusQueryResponseDTO> rangeQuery(PrometheusQueryRequestDTO dto) {
        LOG.debugf("[PROMETHEUS] Range query: %s, start=%s, end=%s, step=%s",
                dto.query(), dto.start(), dto.end(), dto.step());
        return prometheusClient.rangeQuery(dto.query(), dto.start(), dto.end(), dto.step())
                .onItem().invoke(response -> LOG.debugf(
                        "[PROMETHEUS] Range query response: status=%s, resultType=%s",
                        response.status(),
                        response.data() != null ? response.data().resultType() : "null"))
                .onFailure().invoke(e -> LOG.errorf(e, "[PROMETHEUS] Range query failed: %s", dto.query()));
    }

    public Uni<PrometheusLabelsResponseDTO> queryLabels() {
        LOG.debug("[PROMETHEUS] Querying all labels");
        return prometheusClient.queryLabels()
                .onItem().invoke(response -> LOG.debugf(
                        "[PROMETHEUS] Labels response: status=%s, labelCount=%d",
                        response.status(),
                        response.data() != null ? response.data().size() : 0))
                .onFailure().invoke(e -> LOG.errorf(e, "[PROMETHEUS] Query labels failed"));
    }

    public Uni<PrometheusLabelsResponseDTO> labelValues(String labelName) {
        LOG.debugf("[PROMETHEUS] Querying label values for: %s", labelName);
        return prometheusClient.labelValues(labelName)
                .onItem().invoke(response -> LOG.debugf(
                        "[PROMETHEUS] Label values response for '%s': status=%s, valueCount=%d",
                        labelName,
                        response.status(),
                        response.data() != null ? response.data().size() : 0))
                .onFailure().invoke(e -> LOG.errorf(e, "[PROMETHEUS] Query label values failed for: %s", labelName));
    }
}
