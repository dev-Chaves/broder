package org.acme.domain.alarm;

import org.acme.domain.alarm.enums.AlarmSeverity;
import org.acme.domain.alarm.enums.AlarmStatus;
import org.acme.domain.alarm.enums.ComparisonOperator;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AlarmTest {

    @Test
    void shouldCreateAlarmWithCondition() {
        AlarmCondition condition = AlarmCondition.builder()
                .query("up")
                .operator(ComparisonOperator.GT)
                .threshold("0")
                .build();
        Alarm alarm = Alarm.builder()
                .name("Test Alarm")
                .description("A test alarm")
                .condition(condition)
                .build();

        assertEquals("Test Alarm", alarm.getName());
        assertEquals("A test alarm", alarm.getDescription());
        assertEquals(condition, alarm.getCondition());
        assertEquals(AlarmStatus.ACTIVE, alarm.getStatus());
        assertTrue(alarm.isEnabled());
        assertEquals(AlarmSeverity.MEDIUM, alarm.getSeverity());
        assertEquals(0L, alarm.getUsages());
        assertNotNull(alarm.getCreatedAt());
    }

    @Test
    void shouldRejectBlankName() {
        AlarmCondition condition = AlarmCondition.builder()
                .query("up")
                .operator(ComparisonOperator.GT)
                .threshold("0")
                .build();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Alarm.builder()
                        .name("")
                        .description("desc")
                        .condition(condition)
                        .build());
        assertEquals("Name cannot be blank", ex.getMessage());
    }

    @Test
    void shouldRejectNullCondition() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Alarm.builder()
                        .name("Test")
                        .description("desc")
                        .condition(null)
                        .build());
        assertEquals("Condition cannot be null", ex.getMessage());
    }

    // --- evaluate ---

    @Test
    void evaluateShouldReturnFiringWhenConditionMatches() {
        AlarmCondition condition = AlarmCondition.builder()
                .query("up")
                .operator(ComparisonOperator.GT)
                .threshold("0")
                .build();
        Alarm alarm = Alarm.builder()
                .name("Test")
                .description("desc")
                .condition(condition)
                .build();

        assertEquals(AlarmStatus.FIRING, alarm.evaluate(1.0));
    }

    @Test
    void evaluateShouldReturnResolvedWhenWasFiringAndConditionNoLongerMatches() {
        AlarmCondition condition = AlarmCondition.builder()
                .query("up")
                .operator(ComparisonOperator.GT)
                .threshold("0")
                .build();
        Alarm alarm = Alarm.builder()
                .name("Test")
                .description("desc")
                .condition(condition)
                .status(AlarmStatus.FIRING)
                .build();

        assertEquals(AlarmStatus.RESOLVED, alarm.evaluate(-1.0));
    }

    @Test
    void evaluateShouldReturnActiveWhenNotFiringAndConditionDoesNotMatch() {
        AlarmCondition condition = AlarmCondition.builder()
                .query("up")
                .operator(ComparisonOperator.GT)
                .threshold("0")
                .build();
        Alarm alarm = Alarm.builder()
                .name("Test")
                .description("desc")
                .condition(condition)
                .build();

        assertEquals(AlarmStatus.ACTIVE, alarm.evaluate(-1.0));
    }

    @Test
    void evaluateShouldReturnFiringWhenAlreadyFiringAndStillMatches() {
        AlarmCondition condition = AlarmCondition.builder()
                .query("up")
                .operator(ComparisonOperator.GT)
                .threshold("0")
                .build();
        Alarm alarm = Alarm.builder()
                .name("Test")
                .description("desc")
                .condition(condition)
                .status(AlarmStatus.FIRING)
                .build();

        assertEquals(AlarmStatus.FIRING, alarm.evaluate(1.0));
    }

    // --- recordEvaluation ---

    @Test
    void recordEvaluationShouldUpdateStatusAndTimestamp() {
        AlarmCondition condition = AlarmCondition.builder()
                .query("up")
                .operator(ComparisonOperator.GT)
                .threshold("0")
                .build();
        Alarm alarm = Alarm.builder()
                .name("Test")
                .description("desc")
                .condition(condition)
                .build();
        LocalDateTime now = LocalDateTime.now();

        alarm.recordEvaluation(AlarmStatus.FIRING, now);

        assertEquals(AlarmStatus.FIRING, alarm.getStatus());
        assertEquals(now, alarm.getLastEvaluatedAt());
        assertEquals(now, alarm.getLastFiredAt());
        assertEquals(1L, alarm.getUsages());
    }

    @Test
    void recordEvaluationShouldNotIncrementUsagesForNonFiring() {
        AlarmCondition condition = AlarmCondition.builder()
                .query("up")
                .operator(ComparisonOperator.GT)
                .threshold("0")
                .build();
        Alarm alarm = Alarm.builder()
                .name("Test")
                .description("desc")
                .condition(condition)
                .build();
        LocalDateTime now = LocalDateTime.now();

        alarm.recordEvaluation(AlarmStatus.RESOLVED, now);

        assertEquals(0L, alarm.getUsages());
    }

    @Test
    void recordEvaluationShouldIncrementUsagesOnRepeatedFiring() {
        AlarmCondition condition = AlarmCondition.builder()
                .query("up")
                .operator(ComparisonOperator.GT)
                .threshold("0")
                .build();
        Alarm alarm = Alarm.builder()
                .name("Test")
                .description("desc")
                .condition(condition)
                .build();
        LocalDateTime now = LocalDateTime.now();

        alarm.recordEvaluation(AlarmStatus.FIRING, now);
        alarm.recordEvaluation(AlarmStatus.FIRING, now.plusSeconds(1));

        assertEquals(2L, alarm.getUsages());
    }

    // --- transition helpers ---

    @Test
    void wasFiringShouldReturnTrueOnlyWhenStatusIsFiring() {
        AlarmCondition condition = AlarmCondition.builder()
                .query("up")
                .operator(ComparisonOperator.GT)
                .threshold("0")
                .build();
        Alarm alarm = Alarm.builder()
                .name("Test")
                .description("desc")
                .condition(condition)
                .build();

        assertFalse(alarm.wasFiring());

        alarm.recordEvaluation(AlarmStatus.FIRING, LocalDateTime.now());
        assertTrue(alarm.wasFiring());
    }

    @Test
    void isTransitioningToFiringShouldDetectTransition() {
        AlarmCondition condition = AlarmCondition.builder()
                .query("up")
                .operator(ComparisonOperator.GT)
                .threshold("0")
                .build();
        Alarm alarm = Alarm.builder()
                .name("Test")
                .description("desc")
                .condition(condition)
                .build();

        assertTrue(alarm.isTransitioningToFiring(AlarmStatus.FIRING));
        assertFalse(alarm.isTransitioningToFiring(AlarmStatus.RESOLVED));

        alarm.recordEvaluation(AlarmStatus.FIRING, LocalDateTime.now());
        assertFalse(alarm.isTransitioningToFiring(AlarmStatus.FIRING));
    }

    @Test
    void isTransitioningFromFiringShouldDetectTransition() {
        AlarmCondition condition = AlarmCondition.builder()
                .query("up")
                .operator(ComparisonOperator.GT)
                .threshold("0")
                .build();
        Alarm alarm = Alarm.builder()
                .name("Test")
                .description("desc")
                .condition(condition)
                .build();

        assertFalse(alarm.isTransitioningFromFiring(AlarmStatus.RESOLVED));

        alarm.recordEvaluation(AlarmStatus.FIRING, LocalDateTime.now());
        assertTrue(alarm.isTransitioningFromFiring(AlarmStatus.RESOLVED));
        assertFalse(alarm.isTransitioningFromFiring(AlarmStatus.FIRING));
    }

    // --- mutators ---

    @Test
    void renameShouldUpdateName() {
        AlarmCondition condition = AlarmCondition.builder()
                .query("up")
                .operator(ComparisonOperator.GT)
                .threshold("0")
                .build();
        Alarm alarm = Alarm.builder()
                .name("Old")
                .description("desc")
                .condition(condition)
                .build();

        alarm.rename("New");
        assertEquals("New", alarm.getName());
    }

    @Test
    void renameShouldRejectBlank() {
        AlarmCondition condition = AlarmCondition.builder()
                .query("up")
                .operator(ComparisonOperator.GT)
                .threshold("0")
                .build();
        Alarm alarm = Alarm.builder()
                .name("Test")
                .description("desc")
                .condition(condition)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> alarm.rename("  "));
        assertEquals("Name cannot be blank", ex.getMessage());
    }

    @Test
    void changeConditionShouldRejectNull() {
        AlarmCondition condition = AlarmCondition.builder()
                .query("up")
                .operator(ComparisonOperator.GT)
                .threshold("0")
                .build();
        Alarm alarm = Alarm.builder()
                .name("Test")
                .description("desc")
                .condition(condition)
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> alarm.changeCondition(null));
        assertEquals("Condition cannot be null", ex.getMessage());
    }
}
