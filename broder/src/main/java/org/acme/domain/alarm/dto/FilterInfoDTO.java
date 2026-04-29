package org.acme.domain.alarm.dto;

public record FilterInfoDTO(
        String key,
        String label,
        String placeholder,
        String description
) {
}
