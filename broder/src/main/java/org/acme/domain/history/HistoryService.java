package org.acme.domain.history;

import io.quarkus.panache.common.Page;
import jakarta.transaction.Transactional;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.domain.alarm.Alarm;
import org.acme.domain.alarm.AlarmRepository;
import org.acme.domain.alarm.enums.AlarmStatus;
import org.acme.domain.history.dto.HistoryRequestDTO;
import org.acme.domain.history.dto.HistoryResponseDTO;
import org.acme.domain.shared.api.PageResponse;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class HistoryService {

    private static final Logger LOG = Logger.getLogger(HistoryService.class);

    private final HistoryRepository historyRepository;
    private final AlarmRepository alarmRepository;

    public HistoryService(HistoryRepository historyRepository, AlarmRepository alarmRepository) {
        this.historyRepository = historyRepository;
        this.alarmRepository = alarmRepository;
    }

    public PageResponse<HistoryResponseDTO> listAll(int page, int size) {
        LOG.debugf("[HISTORY] Listing all history entries: page=%d, size=%d", page, size);
        var panachePage = Page.of(page, size);
        List<History> entries = historyRepository.findAll().page(panachePage).list();
        long total = historyRepository.count();
        List<HistoryResponseDTO> dtos = entries.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        int totalPages = (int) Math.ceil((double) total / size);
        LOG.debugf("[HISTORY] Found %d total history entries", total);
        return new PageResponse<>(dtos, total, totalPages, page, size);
    }

    public PageResponse<HistoryResponseDTO> listByAlarmId(Long alarmId, int page, int size) {
        LOG.debugf("[HISTORY] Listing history for alarm id=%d: page=%d, size=%d", alarmId, (Object) page, (Object) size);
        var panachePage = Page.of(page, size);
        List<History> entries = historyRepository.findByAlarmId(alarmId, panachePage);
        long total = historyRepository.countByAlarmId(alarmId);
        List<HistoryResponseDTO> dtos = entries.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / size);
        LOG.debugf("[HISTORY] Found %d history entries for alarm id=%d", total, alarmId);
        return new PageResponse<>(dtos, total, totalPages, page, size);
    }

    public HistoryResponseDTO findLatestByAlarmId(Long alarmId) {
        LOG.debugf("[HISTORY] Finding latest history for alarm id=%d", alarmId);
        History history = historyRepository.findLatestByAlarmId(alarmId);
        if (history == null) {
            LOG.warnf("[HISTORY] No history found for alarm id=%d", alarmId);
            throw new jakarta.ws.rs.NotFoundException("No history found for alarm: " + alarmId);
        }
        return toResponseDTO(history);
    }

    @Transactional
    public HistoryResponseDTO save(History history) {
        LOG.infof("[HISTORY] Recording history entry for alarm id=%d, status=%s, value=%s",
                history.getAlarm().getId(), history.getStatus(), history.getValue());

        historyRepository.persist(history);

        LOG.infof("[HISTORY] History entry created: id=%d, alarm='%s', status=%s",
                history.getId(), history.getAlarm().getName(), history.getStatus());

        return toResponseDTO(history);
    }

    @Transactional
    public HistoryResponseDTO save(HistoryRequestDTO dto) {
        LOG.infof("[HISTORY] Recording history entry for alarm id=%d, status=%s, value=%s",
                dto.alarmId(), dto.status(), dto.value());

        Alarm alarm = alarmRepository.findByIdOptional(dto.alarmId())
                .orElseThrow(() -> new IllegalArgumentException("Alarm not found for id: " + dto.alarmId()));

        AlarmStatus status = AlarmStatus.valueOf(dto.status());
        History history = History.recordFor(alarm, status, dto.value());
        historyRepository.persist(history);

        LOG.infof("[HISTORY] History entry created: id=%d, alarm='%s', status=%s",
                history.getId(), alarm.getName(), status);

        return toResponseDTO(history);
    }

    private HistoryResponseDTO toResponseDTO(History history) {
        return new HistoryResponseDTO(
                history.getId(),
                history.getAlarm().getId(),
                history.getAlarm().getName(),
                history.getStatus().name(),
                history.getValue(),
                history.getCreatedAt()
        );
    }
}
