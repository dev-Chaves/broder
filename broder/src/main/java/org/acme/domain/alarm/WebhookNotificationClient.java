package org.acme.domain.alarm;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.domain.alarm.dto.WebhookPayloadDTO;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@ApplicationScoped
public class WebhookNotificationClient {

    private static final Logger LOG = Logger.getLogger(WebhookNotificationClient.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Duration timeout;

    @Inject
    public WebhookNotificationClient(
            ObjectMapper objectMapper,
            @ConfigProperty(name = "broder.webhook.timeout-seconds", defaultValue = "5") int timeoutSeconds) {
        this.objectMapper = objectMapper;
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(this.timeout)
                .build();
    }

    public void send(String webhookUrl, WebhookPayloadDTO payload) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        try {
            String json = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Broder-Webhook/1.0")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                LOG.infof("[WEBHOOK] Sent successfully to %s | status=%d", webhookUrl, response.statusCode());
            }

            if (response.statusCode() < 200 || response.statusCode() > 299) {
                LOG.warnf("[WEBHOOK] Received non-2xx response from %s | status=%d | body=%s",
                        webhookUrl, response.statusCode(), response.body());
            }

        } catch (Exception e) {
            LOG.warnf(e, "[WEBHOOK] Failed to send notification to %s", webhookUrl);
        }
    }
}
