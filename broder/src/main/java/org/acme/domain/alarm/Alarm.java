package org.acme.domain.alarm;

import jakarta.persistence.*;

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

    public Alarm() {
        this.usages = 0L;
    }

    public Alarm(String name, String description, AlarmCondition condition) {
        this.rename(name);
        this.changeDescription(description);
        this.changeCondition(condition);
        this.usages = 0L;
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

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setStatus(AlarmStatus status) {
        this.status = status;
    }

    public void setSeverity(AlarmSeverity severity) {
        this.severity = severity;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public void setEvaluationIntervalSeconds(Integer evaluationIntervalSeconds) {
        this.evaluationIntervalSeconds = evaluationIntervalSeconds;
    }

    public void setLastEvaluatedAt(LocalDateTime lastEvaluatedAt) {
        this.lastEvaluatedAt = lastEvaluatedAt;
    }

    public void setLastFiredAt(LocalDateTime lastFiredAt) {
        this.lastFiredAt = lastFiredAt;
    }

    public void setWebhookUrl(String webhookUrl) {
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

    private String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return value.trim();
    }
}
