package org.acme.domain.history.dto;

import java.time.LocalDateTime;

public record HistoryResponseDTO(
        Long id,
        Long alarmId,
        String alarmName,
        String status,
        Double value,
        LocalDateTime createdAt
) {
}
