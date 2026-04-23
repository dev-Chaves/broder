package org.acme.domain.prometheus.dto;

import jakarta.validation.constraints.NotBlank;

public record PrometheusQueryRequestDTO(
        @NotBlank(message = "PromQL query cannot be blank")
        String query,
        String time,
        String start,
        String end,
        String step
) {
}
