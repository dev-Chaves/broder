package org.acme.domain.alarm.dto;

import java.util.List;

public record MetricInfoDTO(
        String name,
        String type,
        String description,
        List<String> availableLabels,
        String unit
) {
}
