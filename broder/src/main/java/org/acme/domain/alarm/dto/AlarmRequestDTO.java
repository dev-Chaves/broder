package org.acme.domain.alarm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlarmRequestDTO(
        @NotBlank(message = "Name cannot be blank")
        String name,
        String description,
        @NotBlank(message = "Query cannot be blank")
        String query,
        @NotBlank(message = "Comparison cannot be blank")
        String comparison,
        @NotBlank(message = "Threshold cannot be blank")
        String threshold,
        Integer evaluationIntervalSeconds,
        String severity,
        String category,
        String templateId,
        String webhookUrl
) {
}
