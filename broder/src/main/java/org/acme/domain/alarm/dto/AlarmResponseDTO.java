package org.acme.domain.alarm.dto;

import java.time.LocalDateTime;

public record AlarmResponseDTO(
        Long id,
        String name,
        String description,
        String query,
        String comparison,
        String threshold,
        boolean enabled,
        String status,
        String severity,
        String category,
        String templateId,
        Integer evaluationIntervalSeconds,
        LocalDateTime lastEvaluatedAt,
        LocalDateTime lastFiredAt,
        Long usages,
        LocalDateTime createdAt
) {
}
