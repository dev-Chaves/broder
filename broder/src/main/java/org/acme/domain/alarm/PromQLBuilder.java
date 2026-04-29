package org.acme.domain.alarm;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.alarm.dto.AlarmTemplate;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class PromQLBuilder {

    private static final Logger LOG = Logger.getLogger(PromQLBuilder.class);

    public String build(String templateId, Map<String, String> filters) {
        LOG.debugf("[PROMQL] Building query for template=%s, filters=%s", templateId, filters);

        AlarmTemplate template = AlarmTemplateRegistry.findById(templateId);
        String query = template.queryTemplate();

        List<String> supportedKeys = template.filterInfo().stream()
                .map(org.acme.domain.alarm.dto.FilterInfoDTO::key)
                .toList();

        if (filters == null || filters.isEmpty()) {
            throw new IllegalArgumentException("filters cannot be empty");
        }

        String filterStr = filters.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isBlank())
                .map(e -> String.format("%s=\"%s\"", e.getKey(), sanitizeFilterValue(e.getValue())))
                .collect(Collectors.joining(","));

        if (filterStr.isBlank()) {
            throw new IllegalArgumentException("filters cannot be empty");
        }

        String result;
        if (query.contains("rate(") || query.contains("histogram_quantile")) {
            String metricName = extractMetricName(query);
            result = query.replace(metricName, metricName + "{" + filterStr + "}");
        } else if (!query.contains("{") || !query.contains("}")) {
            result = query + "{" + filterStr + "}";
        } else {
            int open = query.indexOf("{");
            int close = query.indexOf("}", open);
            if (close <= open) {
                throw new IllegalArgumentException(String.format("Malformed query braces: %s", query));
            }
            String inner = query.substring(open + 1, close);
            String separator = inner.isBlank() ? "" : ",";
            result = query.substring(0, open + 1) + inner + separator + filterStr + query.substring(close);
        }

        LOG.debugf("[PROMQL] Generated query: %s", result);
        return result;
    }

    private String extractMetricName(String query) {
        if (query.contains("rate(")) {
            int start = query.indexOf("rate(") + 5;
            int end = query.indexOf("[", start);
            if (end > start) {
                return query.substring(start, end).trim();
            }
        }
        if (query.contains("histogram_quantile")) {
            int start = query.indexOf("rate(") + 5;
            int end = query.indexOf("[", start);
            if (end > start) {
                return query.substring(start, end).trim();
            }
        }
        return query.split("[{(]")[0].trim();
    }

    private String sanitizeFilterValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\\\"");
    }
}
