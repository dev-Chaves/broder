package org.acme.domain.history;

import jakarta.persistence.*;
import org.acme.domain.alarm.Alarm;
import org.acme.domain.alarm.enums.AlarmStatus;

import java.time.LocalDateTime;

@Entity
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alarm_id", nullable = false)
    private Alarm alarm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlarmStatus status;

    private Double value;

    private LocalDateTime createdAt = LocalDateTime.now();

    protected History() {
        // JPA
    }

    private History(Alarm alarm, AlarmStatus status, Double value) {
        this.alarm = alarm;
        this.status = status;
        this.value = value;
    }

    public static History recordFor(Alarm alarm, AlarmStatus status, Double value) {
        return new History(alarm, status, value);
    }

    // --- Builder ---

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Alarm alarm;
        private AlarmStatus status;
        private Double value;
        private LocalDateTime createdAt = LocalDateTime.now();

        private Builder() {
        }

        public Builder alarm(Alarm alarm) {
            this.alarm = alarm;
            return this;
        }

        public Builder status(AlarmStatus status) {
            this.status = status;
            return this;
        }

        public Builder value(Double value) {
            this.value = value;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public History build() {
            History history = new History();
            history.alarm = alarm;
            history.status = status;
            history.value = value;
            history.createdAt = createdAt;
            return history;
        }
    }

    // --- Accessors ---

    public Long getId() {
        return id;
    }

    public Alarm getAlarm() {
        return alarm;
    }

    public AlarmStatus getStatus() {
        return status;
    }

    public Double getValue() {
        return value;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
