package org.acme.domain.prometheus.dto;

import java.util.List;
import java.util.Map;

public record PrometheusQueryResponseDTO(
        String status,
        PrometheusDataDTO data,
        String errorType,
        String error
) {
    public record PrometheusDataDTO(
            String resultType,
            List<PrometheusResultDTO> result
    ) {
    }

    public record PrometheusResultDTO(
            Map<String, String> metric,
            List<List<Object>> values,
            List<Object> value
    ) {
    }
}
