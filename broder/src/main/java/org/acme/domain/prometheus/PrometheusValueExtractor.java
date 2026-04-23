package org.acme.domain.prometheus;

import org.acme.domain.prometheus.dto.PrometheusQueryResponseDTO;
import org.jboss.logging.Logger;

import java.util.List;

public final class PrometheusValueExtractor {

    private static final Logger LOG = Logger.getLogger(PrometheusValueExtractor.class);

    private PrometheusValueExtractor() {
        // utility class
    }

    public static double extract(PrometheusQueryResponseDTO response) {
        if (response == null || response.data() == null || response.data().result() == null) {
            LOG.debug("[EXTRACTOR] No data in Prometheus response, returning 0.0");
            return 0.0;
        }
        return extract(response.data().result());
    }

    public static double extract(List<PrometheusQueryResponseDTO.PrometheusResultDTO> results) {
        if (results == null || results.isEmpty()) {
            LOG.debug("[EXTRACTOR] No results from Prometheus query, returning 0.0");
            return 0.0;
        }
        var value = results.get(0).value();
        if (value == null || value.size() < 2) {
            LOG.debug("[EXTRACTOR] Result value is null or incomplete, returning 0.0");
            return 0.0;
        }
        try {
            double parsed = Double.parseDouble(value.get(1).toString());
            LOG.debugf("[EXTRACTOR] Extracted value: %.6f from raw: %s", parsed, value.get(1));
            return parsed;
        } catch (NumberFormatException e) {
            LOG.warnf("[EXTRACTOR] Failed to parse value '%s' as double, returning 0.0", value.get(1));
            return 0.0;
        }
    }
}
