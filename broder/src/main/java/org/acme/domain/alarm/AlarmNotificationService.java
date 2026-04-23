package org.acme.domain.alarm;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AlarmNotificationService {

    private static final Logger LOG = Logger.getLogger(AlarmNotificationService.class);

    public void notifyFiring(Alarm alarm, double currentValue) {
        AlarmCondition c = alarm.getCondition();
        LOG.infof("🚨 ALARM FIRING: %s (ID: %d) | Current: %.4f %s Threshold: %s | Query: %s",
                alarm.getName(), alarm.getId(), currentValue,
                c.operator().symbol(), c.threshold(), c.query());
    }

    public void notifyResolved(Alarm alarm, double currentValue) {
        LOG.infof("✅ ALARM RESOLVED: %s (ID: %d) | Current: %.4f | Query: %s",
                alarm.getName(), alarm.getId(), currentValue, alarm.getCondition().query());
    }

    public void notifyError(Alarm alarm, Throwable error) {
        LOG.errorf("❌ ALARM ERROR: %s (ID: %d) | %s", alarm.getName(), alarm.getId(), error.getMessage());
    }
}
