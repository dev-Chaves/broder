package org.acme.domain.alarm;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.alarm.dto.*;
import org.acme.domain.prometheus.PrometheusService;
import org.acme.domain.prometheus.PrometheusValueExtractor;
import org.acme.domain.prometheus.dto.PrometheusQueryRequestDTO;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class AlarmBuilderService {

    private static final Logger LOG = Logger.getLogger(AlarmBuilderService.class);

    private final PrometheusService prometheusService;
    private final PromQLBuilder promQLBuilder;
    private final MetricCatalog metricCatalog;
    private final AlarmService alarmService;

    public AlarmBuilderService(PrometheusService prometheusService,
                               PromQLBuilder promQLBuilder,
                               MetricCatalog metricCatalog,
                               AlarmService alarmService) {
        this.prometheusService = prometheusService;
        this.promQLBuilder = promQLBuilder;
        this.metricCatalog = metricCatalog;
        this.alarmService = alarmService;
    }

    public AlarmTemplate findTemplate(String templateId) {
        LOG.debugf("[BUILDER] Looking up template: %s", templateId);
        return AlarmTemplateRegistry.findById(templateId);
    }

    public List<AlarmTemplate> listTemplates() {
        LOG.debug("[BUILDER] Listing all templates");
        return AlarmTemplateRegistry.all();
    }

    public List<String> listCategories() {
        LOG.debug("[BUILDER] Listing categories");
        return AlarmTemplateRegistry.categories();
    }

    public List<MetricInfoDTO> listMetrics() {
        LOG.debug("[BUILDER] Listing available metrics");
        return metricCatalog.listMetrics();
    }

    public AlarmPreviewResponseDTO preview(AlarmBuilderRequestDTO dto) {
        LOG.infof("[BUILDER] Preview alarm: template=%s, filters=%s", dto.templateId(), dto.filters());

        AlarmTemplate template = AlarmTemplateRegistry.findById(dto.templateId());
        String query = promQLBuilder.build(dto.templateId(), dto.filters());
        String severity = dto.severity() != null ? dto.severity() : template.defaultSeverity();

        try {
            var request = new PrometheusQueryRequestDTO(query, null, null, null, null);
            var response = prometheusService.instantQuery(request)
                    .await().atMost(java.time.Duration.ofSeconds(10));

            double currentValue = PrometheusValueExtractor.extract(response);
            boolean wouldTrigger = ComparisonOperator.fromSymbol(dto.comparison())
                    .apply(currentValue, Double.parseDouble(dto.threshold()));

            LOG.infof("[BUILDER] Preview result: currentValue=%.6f, wouldTrigger=%s", currentValue, wouldTrigger);

            return new AlarmPreviewResponseDTO(
                    query,
                    currentValue,
                    dto.threshold(),
                    dto.comparison(),
                    wouldTrigger,
                    severity,
                    template.category(),
                    response.status(),
                    LocalDateTime.now().toString()
            );
        } catch (Exception e) {
            LOG.errorf(e, "[BUILDER] Preview failed for query: %s", query);
            return new AlarmPreviewResponseDTO(
                    query,
                    null,
                    dto.threshold(),
                    dto.comparison(),
                    null,
                    severity,
                    template.category(),
                    "ERROR",
                    LocalDateTime.now().toString()
            );
        }
    }

    public AlarmPreviewResponseDTO validate(AlarmBuilderRequestDTO dto) {
        LOG.infof("[BUILDER] Validating alarm: template=%s", dto.templateId());

        try {
            AlarmTemplateRegistry.findById(dto.templateId());
        } catch (IllegalArgumentException e) {
            LOG.warnf("[BUILDER] Invalid template ID: %s", dto.templateId());
            throw new IllegalArgumentException("Invalid template ID: " + dto.templateId());
        }

        String query = promQLBuilder.build(dto.templateId(), dto.filters());
        AlarmTemplate template = AlarmTemplateRegistry.findById(dto.templateId());
        String severity = dto.severity() != null ? dto.severity() : template.defaultSeverity();

        return new AlarmPreviewResponseDTO(
                query,
                null,
                dto.threshold(),
                dto.comparison(),
                null,
                severity,
                template.category(),
                "VALIDATED",
                LocalDateTime.now().toString()
        );
    }

    public AlarmResponseDTO buildAndSave(AlarmBuilderRequestDTO dto) {
        LOG.infof("[BUILDER] Building and saving alarm: template=%s, name='%s'", dto.templateId(), dto.name());

        var preview = validate(dto);
        AlarmTemplate template = findTemplate(dto.templateId());

        var alarmRequest = new AlarmRequestDTO(
                dto.name(),
                dto.description(),
                preview.generatedQuery(),
                dto.comparison(),
                dto.threshold(),
                dto.evaluationIntervalSeconds(),
                preview.severity(),
                preview.category(),
                dto.templateId()
        );

        var saved = alarmService.save(alarmRequest);
        LOG.infof("[BUILDER] Alarm built and saved: id=%d, name='%s', template=%s", saved.id(), saved.name(), dto.templateId());
        return saved;
    }
}
