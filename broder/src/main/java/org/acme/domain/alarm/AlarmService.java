package org.acme.domain.alarm;

import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.acme.domain.alarm.dto.AlarmRequestDTO;
import org.acme.domain.alarm.dto.AlarmResponseDTO;
import org.acme.domain.alarm.dto.AlarmUpdateDTO;
import org.acme.domain.shared.api.PageResponse;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class AlarmService {

    private static final Logger LOG = Logger.getLogger(AlarmService.class);

    private final AlarmRepository alarmRepository;

    public AlarmService(AlarmRepository alarmRepository) {
        this.alarmRepository = alarmRepository;
    }

    @Transactional
    public AlarmResponseDTO save(AlarmRequestDTO dto) {
        AlarmCondition condition = buildCondition(dto.query(), dto.comparison(), dto.threshold());
        LOG.infof("[SERVICE] Creating new alarm: name='%s', condition=%s", dto.name(), condition);

        Alarm alarm = new Alarm(dto.name(), dto.description(), condition);
        if (dto.evaluationIntervalSeconds() != null) {
            alarm.setEvaluationIntervalSeconds(dto.evaluationIntervalSeconds());
        }
        if (dto.severity() != null) {
            alarm.setSeverity(AlarmSeverity.valueOf(dto.severity()));
        }
        if (dto.category() != null) {
            alarm.setCategory(dto.category());
        }
        if (dto.templateId() != null) {
            alarm.setTemplateId(dto.templateId());
        }
        alarmRepository.persist(alarm);

        LOG.infof("[SERVICE] Alarm created successfully: id=%d, name='%s'", alarm.getId(), alarm.getName());
        return toResponseDTO(alarm);
    }

    public PageResponse<AlarmResponseDTO> listAll(int page, int size) {
        LOG.debugf("[SERVICE] Listing all alarms: page=%d, size=%d", page, size);
        var panachePage = Page.of(page, size);
        List<Alarm> alarms = alarmRepository.findAllPaged(panachePage);
        long total = alarmRepository.count();
        List<AlarmResponseDTO> dtos = alarms.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        int totalPages = (int) Math.ceil((double) total / size);
        LOG.debugf("[SERVICE] Found %d total alarm(s)", total);
        return new PageResponse<>(dtos, total, totalPages, page, size);
    }

    public List<Alarm> listEnabledAlarms() {
        LOG.debug("[SERVICE] Fetching enabled alarms");
        List<Alarm> alarms = alarmRepository.list("enabled", true);
        LOG.debugf("[SERVICE] Found %d enabled alarm(s)", alarms.size());
        return alarms;
    }

    public AlarmResponseDTO findById(Long id) {
        LOG.debugf("[SERVICE] Finding alarm by id=%d", id);
        Alarm alarm = alarmRepository.findById(id);
        if (alarm == null) {
            LOG.warnf("[SERVICE] Alarm not found: id=%d", id);
            throw new jakarta.ws.rs.NotFoundException("Alarm not found: " + id);
        }
        LOG.debugf("[SERVICE] Alarm found: id=%d, name='%s'", alarm.getId(), alarm.getName());
        return toResponseDTO(alarm);
    }

    @Transactional
    public AlarmResponseDTO update(Long id, AlarmUpdateDTO dto) {
        LOG.infof("[SERVICE] Updating alarm id=%d", id);
        Alarm alarm = alarmRepository.findById(id);
        if (alarm == null) {
            LOG.warnf("[SERVICE] Cannot update: alarm not found id=%d", id);
            throw new jakarta.ws.rs.NotFoundException("Alarm not found: " + id);
        }

        if (dto.name() != null) {
            LOG.debugf("[SERVICE] Updating name: '%s' -> '%s'", alarm.getName(), dto.name());
            alarm.rename(dto.name());
        }
        if (dto.description() != null) alarm.changeDescription(dto.description());
        if (dto.query() != null || dto.comparison() != null || dto.threshold() != null) {
            AlarmCondition current = alarm.getCondition();
            String newQuery = dto.query() != null ? dto.query() : current.query();
            String newComparison = dto.comparison() != null ? dto.comparison() : current.operator().symbol();
            String newThreshold = dto.threshold() != null ? dto.threshold() : current.threshold();
            alarm.changeCondition(buildCondition(newQuery, newComparison, newThreshold));
        }
        if (dto.evaluationIntervalSeconds() != null) alarm.setEvaluationIntervalSeconds(dto.evaluationIntervalSeconds());
        if (dto.enabled() != null) alarm.setEnabled(dto.enabled());
        if (dto.severity() != null) alarm.setSeverity(AlarmSeverity.valueOf(dto.severity()));

        return toResponseDTO(alarm);
    }

    @Transactional
    public void delete(Long id) {
        LOG.infof("[SERVICE] Deleting alarm id=%d", id);
        Alarm alarm = alarmRepository.findById(id);
        if (alarm == null) {
            LOG.warnf("[SERVICE] Cannot delete: alarm not found id=%d", id);
            throw new jakarta.ws.rs.NotFoundException("Alarm not found: " + id);
        }
        alarmRepository.delete(alarm);
        LOG.infof("[SERVICE] Alarm id=%d deleted successfully", id);
    }

    @Transactional
    public void recordEvaluation(Long id, AlarmStatus newStatus, LocalDateTime evaluatedAt) {
        LOG.debugf("[SERVICE] Recording evaluation for alarm id=%d: status=%s, evaluatedAt=%s",
                id, newStatus, evaluatedAt);
        Alarm alarm = alarmRepository.findById(id);
        if (alarm == null) {
            LOG.warnf("[SERVICE] Cannot record evaluation: alarm not found id=%d", id);
            return;
        }

        AlarmStatus previousStatus = alarm.getStatus();
        alarm.recordEvaluation(newStatus, evaluatedAt);

        if (newStatus == AlarmStatus.FIRING && previousStatus != AlarmStatus.FIRING) {
            LOG.infof("[SERVICE] Alarm id=%d transitioned to FIRING (previous: %s)", id, previousStatus);
        } else if (previousStatus == AlarmStatus.FIRING && newStatus == AlarmStatus.RESOLVED) {
            LOG.infof("[SERVICE] Alarm id=%d transitioned from FIRING to RESOLVED", id);
        } else if (newStatus == AlarmStatus.ERROR) {
            LOG.warnf("[SERVICE] Alarm id=%d evaluation resulted in ERROR (previous: %s)", id, previousStatus);
        }
    }

    @Transactional
    public void seedDefaultAlarms() {
        long existingCount = alarmRepository.count();
        LOG.infof("[SERVICE] Seeding default alarms. Existing count: %d", existingCount);

        if (existingCount > 0) {
            LOG.info("[SERVICE] Database already has alarms, skipping seed");
            return;
        }

        LOG.info("[SERVICE] Creating 4 default alarms...");

        alarmRepository.persist(new Alarm(
                "CPU Usage High",
                "Alert when CPU usage exceeds 80%",
                new AlarmCondition("process_cpu_usage", ComparisonOperator.GT, "0.8")
        ));
        LOG.debug("[SERVICE] Created default alarm: CPU Usage High");

        alarmRepository.persist(new Alarm(
                "Memory Usage High",
                "Alert when heap memory usage exceeds 85%",
                new AlarmCondition(
                        "(jvm_memory_used_bytes{area=\"heap\"} / jvm_memory_max_bytes{area=\"heap\"})",
                        ComparisonOperator.GT,
                        "0.85"
                )
        ));
        LOG.debug("[SERVICE] Created default alarm: Memory Usage High");

        alarmRepository.persist(new Alarm(
                "HTTP 5xx Errors",
                "Alert when 5xx error rate exceeds 5% per minute",
                new AlarmCondition(
                        "rate(http_server_requests_seconds_count{outcome=\"SERVER_ERROR\"}[1m])",
                        ComparisonOperator.GT,
                        "0.05"
                )
        ));
        LOG.debug("[SERVICE] Created default alarm: HTTP 5xx Errors");

        alarmRepository.persist(new Alarm(
                "HTTP 4xx Errors",
                "Alert when 4xx error rate exceeds 10% per minute",
                new AlarmCondition(
                        "rate(http_server_requests_seconds_count{outcome=\"CLIENT_ERROR\"}[1m])",
                        ComparisonOperator.GT,
                        "0.1"
                )
        ));
        LOG.debug("[SERVICE] Created default alarm: HTTP 4xx Errors");

        LOG.infof("[SERVICE] Default alarms seeded successfully. Total: %d", alarmRepository.count());
    }

    private AlarmCondition buildCondition(String query, String comparison, String threshold) {
        return new AlarmCondition(query, ComparisonOperator.fromSymbol(comparison), threshold);
    }

    private AlarmResponseDTO toResponseDTO(Alarm alarm) {
        AlarmCondition c = alarm.getCondition();
        return new AlarmResponseDTO(
                alarm.getId(),
                alarm.getName(),
                alarm.getDescription(),
                c != null ? c.query() : null,
                c != null ? c.operator().symbol() : null,
                c != null ? c.threshold() : null,
                alarm.isEnabled(),
                alarm.getStatus().name(),
                alarm.getSeverity() != null ? alarm.getSeverity().name() : null,
                alarm.getCategory(),
                alarm.getTemplateId(),
                alarm.getEvaluationIntervalSeconds(),
                alarm.getLastEvaluatedAt(),
                alarm.getLastFiredAt(),
                alarm.getUsages(),
                alarm.getCreatedAt()
        );
    }
}
