package org.acme.domain.prometheus.dto;

import java.util.List;

public record PrometheusLabelsResponseDTO(
        String status,
        List<String> data,
        String errorType,
        String error
) {
}
