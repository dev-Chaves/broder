package org.acme.domain.history;

import jakarta.persistence.*;
import org.acme.domain.alarm.Alarm;
import org.acme.domain.alarm.AlarmStatus;

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

    public History() {
    }

    public History(Alarm alarm, AlarmStatus status, Double value) {
        this.alarm = alarm;
        this.status = status;
        this.value = value;
    }

    public static History recordFor(Alarm alarm, AlarmStatus status, Double value) {
        return new History(alarm, status, value);
    }

    public Long getId() {
        return id;
    }

    private void setId(Long id) {
        this.id = id;
    }

    public Alarm getAlarm() {
        return alarm;
    }

    private void setAlarm(Alarm alarm) {
        this.alarm = alarm;
    }

    public AlarmStatus getStatus() {
        return status;
    }

    public void setStatus(AlarmStatus status) {
        this.status = status;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
