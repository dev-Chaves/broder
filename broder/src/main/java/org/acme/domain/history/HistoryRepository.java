package org.acme.domain.history;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class HistoryRepository implements PanacheRepository<History> {

    public List<History> findByAlarmId(Long alarmId, Page page) {
        return find("alarm.id", Sort.by("createdAt").descending(), alarmId)
                .page(page)
                .list();
    }

    public long countByAlarmId(Long alarmId) {
        return count("alarm.id", alarmId);
    }

    public History findLatestByAlarmId(Long alarmId) {
        return find("alarm.id", Sort.by("createdAt").descending(), alarmId)
                .firstResult();
    }
}
