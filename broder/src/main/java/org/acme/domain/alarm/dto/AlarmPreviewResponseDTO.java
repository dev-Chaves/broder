package org.acme.domain.alarm.dto;

public record AlarmPreviewResponseDTO(
        String generatedQuery,
        Double currentValue,
        String threshold,
        String comparison,
        Boolean wouldTrigger,
        String severity,
        String category,
        String status,
        String evaluatedAt
) {
}
