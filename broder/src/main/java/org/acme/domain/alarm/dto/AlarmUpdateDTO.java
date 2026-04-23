package org.acme.domain.alarm.dto;

import jakarta.validation.constraints.NotBlank;

public record AlarmUpdateDTO(
        String name,
        String description,
        String query,
        String comparison,
        String threshold,
        Integer evaluationIntervalSeconds,
        Boolean enabled,
        String severity
) {
}
