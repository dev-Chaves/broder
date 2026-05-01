package org.acme.domain.alarm;

import jakarta.persistence.*;
import org.acme.domain.alarm.enums.AlarmSeverity;
import org.acme.domain.alarm.enums.AlarmStatus;

import java.time.LocalDateTime;

@Entity
public class Alarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private String description;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "query", column = @Column(name = "query")),
            @AttributeOverride(name = "operator", column = @Column(name = "comparison")),
            @AttributeOverride(name = "threshold", column = @Column(name = "threshold"))
    })
    private AlarmCondition condition;

    private boolean enabled = true;

    @Enumerated(EnumType.STRING)
    private AlarmStatus status = AlarmStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    private AlarmSeverity severity = AlarmSeverity.MEDIUM;

    private String category;

    private String templateId;

    private Integer evaluationIntervalSeconds = 20;

    private LocalDateTime lastEvaluatedAt;

    private LocalDateTime lastFiredAt;

    private Long usages;

    private String webhookUrl;

    private LocalDateTime createdAt = LocalDateTime.now();

    protected Alarm() {
        this.usages = 0L;
    }

    // --- Builder ---

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String description;
        private AlarmCondition condition;
        private boolean enabled = true;
        private AlarmStatus status = AlarmStatus.ACTIVE;
        private AlarmSeverity severity = AlarmSeverity.MEDIUM;
        private String category;
        private String templateId;
        private Integer evaluationIntervalSeconds = 20;
        private LocalDateTime lastEvaluatedAt;
        private LocalDateTime lastFiredAt;
        private Long usages = 0L;
        private String webhookUrl;
        private LocalDateTime createdAt = LocalDateTime.now();

        private Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder condition(AlarmCondition condition) {
            this.condition = condition;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder status(AlarmStatus status) {
            this.status = status;
            return this;
        }

        public Builder severity(AlarmSeverity severity) {
            this.severity = severity;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder templateId(String templateId) {
            this.templateId = templateId;
            return this;
        }

        public Builder evaluationIntervalSeconds(Integer evaluationIntervalSeconds) {
            this.evaluationIntervalSeconds = evaluationIntervalSeconds;
            return this;
        }

        public Builder lastEvaluatedAt(LocalDateTime lastEvaluatedAt) {
            this.lastEvaluatedAt = lastEvaluatedAt;
            return this;
        }

        public Builder lastFiredAt(LocalDateTime lastFiredAt) {
            this.lastFiredAt = lastFiredAt;
            return this;
        }

        public Builder usages(Long usages) {
            this.usages = usages;
            return this;
        }

        public Builder webhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Alarm build() {
            Alarm alarm = new Alarm();
            alarm.name = requireNonBlank(name, "Name");
            if (description != null && !description.isBlank()) {
                alarm.description = description.trim();
            }
            if (condition == null) {
                throw new IllegalArgumentException("Condition cannot be null");
            }
            alarm.condition = condition;
            alarm.enabled = enabled;
            alarm.status = status;
            alarm.severity = severity;
            alarm.category = category;
            alarm.templateId = templateId;
            alarm.evaluationIntervalSeconds = evaluationIntervalSeconds;
            alarm.lastEvaluatedAt = lastEvaluatedAt;
            alarm.lastFiredAt = lastFiredAt;
            alarm.usages = usages;
            alarm.webhookUrl = webhookUrl;
            alarm.createdAt = createdAt;
            return alarm;
        }
    }

    // --- Domain behaviour ---

    public AlarmStatus evaluate(double currentValue) {
        if (condition.matches(currentValue)) {
            return AlarmStatus.FIRING;
        }
        if (this.status == AlarmStatus.FIRING) {
            return AlarmStatus.RESOLVED;
        }
        return AlarmStatus.ACTIVE;
    }

    public void recordEvaluation(AlarmStatus newStatus, LocalDateTime evaluatedAt) {
        this.lastEvaluatedAt = evaluatedAt;
        this.status = newStatus;
        if (newStatus == AlarmStatus.FIRING) {
            this.lastFiredAt = evaluatedAt;
            this.usages = (this.usages == null) ? 1L : this.usages + 1;
        }
    }

    public boolean wasFiring() {
        return this.status == AlarmStatus.FIRING;
    }

    public boolean isTransitioningToFiring(AlarmStatus newStatus) {
        return newStatus == AlarmStatus.FIRING && !wasFiring();
    }

    public boolean isTransitioningFromFiring(AlarmStatus newStatus) {
        return wasFiring() && newStatus == AlarmStatus.RESOLVED;
    }

    // --- Mutators ---

    public void rename(String name) {
        this.name = requireNonBlank(name, "Name");
    }

    public void changeDescription(String description) {
        if (description != null && !description.isBlank()) {
            this.description = description.trim();
        }
    }

    public void changeCondition(AlarmCondition condition) {
        if (condition == null) {
            throw new IllegalArgumentException("Condition cannot be null");
        }
        this.condition = condition;
    }

    public void toggleEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void changeSeverity(AlarmSeverity severity) {
        this.severity = severity;
    }

    public void changeCategory(String category) {
        this.category = category;
    }

    public void changeTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public void changeEvaluationIntervalSeconds(Integer evaluationIntervalSeconds) {
        this.evaluationIntervalSeconds = evaluationIntervalSeconds;
    }

    public void changeWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    // --- Accessors ---

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public AlarmCondition getCondition() {
        return condition;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public AlarmStatus getStatus() {
        return status;
    }

    public AlarmSeverity getSeverity() {
        return severity;
    }

    public String getCategory() {
        return category;
    }

    public String getTemplateId() {
        return templateId;
    }

    public Integer getEvaluationIntervalSeconds() {
        return evaluationIntervalSeconds;
    }

    public LocalDateTime getLastEvaluatedAt() {
        return lastEvaluatedAt;
    }

    public LocalDateTime getLastFiredAt() {
        return lastFiredAt;
    }

    public Long getUsages() {
        return usages;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // --- Private helpers ---

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return value.trim();
    }
}
