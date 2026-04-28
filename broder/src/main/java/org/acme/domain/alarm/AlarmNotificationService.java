package org.acme.domain.alarm;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.domain.alarm.dto.WebhookPayloadDTO;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;

@ApplicationScoped
public class AlarmNotificationService {

    private static final Logger LOG = Logger.getLogger(AlarmNotificationService.class);

    private final WebhookNotificationClient webhookClient;

    @Inject
    public AlarmNotificationService(WebhookNotificationClient webhookClient) {
        this.webhookClient = webhookClient;
    }

    public void notifyFiring(Alarm alarm, double currentValue) {
        AlarmCondition c = alarm.getCondition();
        LOG.infof("🚨 ALARM FIRING: %s (ID: %d) | Current: %.4f %s Threshold: %s | Query: %s",
                alarm.getName(), alarm.getId(), currentValue,
                c.operator().symbol(), c.threshold(), c.query());

        sendWebhook(alarm, currentValue, "FIRING",
                String.format("Alarm '%s' is firing. Current value: %.4f %s threshold: %s",
                        alarm.getName(), currentValue, c.operator().symbol(), c.threshold()));
    }

    public void notifyResolved(Alarm alarm, double currentValue) {
        LOG.infof("✅ ALARM RESOLVED: %s (ID: %d) | Current: %.4f | Query: %s",
                alarm.getName(), alarm.getId(), currentValue, alarm.getCondition().query());

        sendWebhook(alarm, currentValue, "RESOLVED",
                String.format("Alarm '%s' has been resolved. Current value: %.4f", alarm.getName(), currentValue));
    }

    public void notifyError(Alarm alarm, Throwable error) {
        LOG.errorf("❌ ALARM ERROR: %s (ID: %d) | %s", alarm.getName(), alarm.getId(), error.getMessage());

        sendWebhook(alarm, null, "ERROR",
                String.format("Alarm '%s' encountered an error: %s", alarm.getName(), error.getMessage()));
    }

    private void sendWebhook(Alarm alarm, Double currentValue, String status, String message) {
        if (alarm.getWebhookUrl() == null || alarm.getWebhookUrl().isBlank()) {
            return;
        }

        WebhookPayloadDTO payload = new WebhookPayloadDTO(
                alarm.getId(),
                alarm.getName(),
                status,
                alarm.getSeverity() != null ? alarm.getSeverity().name() : null,
                currentValue,
                message,
                LocalDateTime.now()
        );

        webhookClient.send(alarm.getWebhookUrl(), payload);
    }
}
