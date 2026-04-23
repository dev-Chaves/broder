package org.acme.domain.history.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HistoryRequestDTO(
        @NotNull(message = "Alarm id cannot be null")
        Long alarmId,
        @NotBlank(message = "Status cannot be blank")
        String status,
        Double value
) {
}
