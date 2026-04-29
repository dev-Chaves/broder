package org.acme.domain.alarm.dto;

import java.util.List;

public record AlarmTemplate(
        String id,
        String name,
        String description,
        String category,
        String defaultSeverity,
        String defaultComparison,
        String defaultThreshold,
        List<FilterInfoDTO> filterInfo,
        String unit,
        String queryTemplate
) {
}
