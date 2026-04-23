package org.acme.domain.alarm;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class AlarmRepository implements PanacheRepository<Alarm> {

    public List<Alarm> findAllPaged(Page page) {
        return findAll(Sort.by("createdAt").descending())
                .page(page)
                .list();
    }
}
