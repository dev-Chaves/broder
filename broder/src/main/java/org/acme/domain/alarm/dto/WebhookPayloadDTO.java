package org.acme.domain.alarm.dto;

import java.time.LocalDateTime;

public record WebhookPayloadDTO(
        Long alarmId,
        String alarmName,
        String status,
        String severity,
        Double currentValue,
        String message,
        LocalDateTime timestamp
) {
}
