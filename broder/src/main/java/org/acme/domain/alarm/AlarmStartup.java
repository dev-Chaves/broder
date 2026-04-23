package org.acme.domain.alarm;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AlarmStartup {

    private static final Logger LOG = Logger.getLogger(AlarmStartup.class);

    private final AlarmService alarmService;

    public AlarmStartup(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    void onStart(@Observes StartupEvent ev) {
        LOG.info("[STARTUP] Application starting up - checking alarm seed data");
        try {
            alarmService.seedDefaultAlarms();
            LOG.info("[STARTUP] Alarm seeding process completed successfully");
        } catch (Exception e) {
            LOG.error("[STARTUP] Failed to seed default alarms", e);
        }
    }
}
