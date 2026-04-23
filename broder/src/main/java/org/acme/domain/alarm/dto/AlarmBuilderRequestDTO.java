package org.acme.domain.alarm.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record AlarmBuilderRequestDTO(
        @NotBlank(message = "Name cannot be blank")
        String name,
        String description,
        @NotBlank(message = "Template ID cannot be blank")
        String templateId,
        Map<String, String> filters,
        @NotBlank(message = "Comparison cannot be blank")
        String comparison,
        @NotBlank(message = "Threshold cannot be blank")
        String threshold,
        String severity,
        Integer evaluationIntervalSeconds
) {
}
