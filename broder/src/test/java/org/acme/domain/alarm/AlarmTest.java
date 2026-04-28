package org.acme.domain.alarm;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AlarmTest {

    @Test
    void shouldCreateAlarmWithCondition() {
        AlarmCondition condition = new AlarmCondition("up", ComparisonOperator.GT, "0");
        Alarm alarm = new Alarm("Test Alarm", "A test alarm", condition);

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
        AlarmCondition condition = new AlarmCondition("up", ComparisonOperator.GT, "0");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Alarm("", "desc", condition));
        assertEquals("Name cannot be blank", ex.getMessage());
    }

    @Test
    void shouldRejectNullCondition() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Alarm("Test", "desc", null));
        assertEquals("Condition cannot be null", ex.getMessage());
    }

    // --- evaluate ---

    @Test
    void evaluateShouldReturnFiringWhenConditionMatches() {
        AlarmCondition condition = new AlarmCondition("up", ComparisonOperator.GT, "0");
        Alarm alarm = new Alarm("Test", "desc", condition);

        assertEquals(AlarmStatus.FIRING, alarm.evaluate(1.0));
    }

    @Test
    void evaluateShouldReturnResolvedWhenWasFiringAndConditionNoLongerMatches() {
        AlarmCondition condition = new AlarmCondition("up", ComparisonOperator.GT, "0");
        Alarm alarm = new Alarm("Test", "desc", condition);
        alarm.setStatus(AlarmStatus.FIRING);

        assertEquals(AlarmStatus.RESOLVED, alarm.evaluate(-1.0));
    }

    @Test
    void evaluateShouldReturnActiveWhenNotFiringAndConditionDoesNotMatch() {
        AlarmCondition condition = new AlarmCondition("up", ComparisonOperator.GT, "0");
        Alarm alarm = new Alarm("Test", "desc", condition);
        alarm.setStatus(AlarmStatus.ACTIVE);

        assertEquals(AlarmStatus.ACTIVE, alarm.evaluate(-1.0));
    }

    @Test
    void evaluateShouldReturnFiringWhenAlreadyFiringAndStillMatches() {
        AlarmCondition condition = new AlarmCondition("up", ComparisonOperator.GT, "0");
        Alarm alarm = new Alarm("Test", "desc", condition);
        alarm.setStatus(AlarmStatus.FIRING);

        assertEquals(AlarmStatus.FIRING, alarm.evaluate(1.0));
    }

    // --- recordEvaluation ---

    @Test
    void recordEvaluationShouldUpdateStatusAndTimestamp() {
        AlarmCondition condition = new AlarmCondition("up", ComparisonOperator.GT, "0");
        Alarm alarm = new Alarm("Test", "desc", condition);
        LocalDateTime now = LocalDateTime.now();

        alarm.recordEvaluation(AlarmStatus.FIRING, now);

        assertEquals(AlarmStatus.FIRING, alarm.getStatus());
        assertEquals(now, alarm.getLastEvaluatedAt());
        assertEquals(now, alarm.getLastFiredAt());
        assertEquals(1L, alarm.getUsages());
    }

    @Test
    void recordEvaluationShouldNotIncrementUsagesForNonFiring() {
        AlarmCondition condition = new AlarmCondition("up", ComparisonOperator.GT, "0");
        Alarm alarm = new Alarm("Test", "desc", condition);
        LocalDateTime now = LocalDateTime.now();

        alarm.recordEvaluation(AlarmStatus.RESOLVED, now);

        assertEquals(0L, alarm.getUsages());
    }

    @Test
    void recordEvaluationShouldIncrementUsagesOnRepeatedFiring() {
        AlarmCondition condition = new AlarmCondition("up", ComparisonOperator.GT, "0");
        Alarm alarm = new Alarm("Test", "desc", condition);
        LocalDateTime now = LocalDateTime.now();

        alarm.recordEvaluation(AlarmStatus.FIRING, now);
        alarm.recordEvaluation(AlarmStatus.FIRING, now.plusSeconds(1));

        assertEquals(2L, alarm.getUsages());
    }

    // --- transition helpers ---

    @Test
    void wasFiringShouldReturnTrueOnlyWhenStatusIsFiring() {
        AlarmCondition condition = new AlarmCondition("up", ComparisonOperator.GT, "0");
        Alarm alarm = new Alarm("Test", "desc", condition);

        assertFalse(alarm.wasFiring());

        alarm.setStatus(AlarmStatus.FIRING);
        assertTrue(alarm.wasFiring());
    }

    @Test
    void isTransitioningToFiringShouldDetectTransition() {
        AlarmCondition condition = new AlarmCondition("up", ComparisonOperator.GT, "0");
        Alarm alarm = new Alarm("Test", "desc", condition);

        assertTrue(alarm.isTransitioningToFiring(AlarmStatus.FIRING));
        assertFalse(alarm.isTransitioningToFiring(AlarmStatus.RESOLVED));

        alarm.setStatus(AlarmStatus.FIRING);
        assertFalse(alarm.isTransitioningToFiring(AlarmStatus.FIRING));
    }

    @Test
    void isTransitioningFromFiringShouldDetectTransition() {
        AlarmCondition condition = new AlarmCondition("up", ComparisonOperator.GT, "0");
        Alarm alarm = new Alarm("Test", "desc", condition);

        assertFalse(alarm.isTransitioningFromFiring(AlarmStatus.RESOLVED));

        alarm.setStatus(AlarmStatus.FIRING);
        assertTrue(alarm.isTransitioningFromFiring(AlarmStatus.RESOLVED));
        assertFalse(alarm.isTransitioningFromFiring(AlarmStatus.FIRING));
    }

    // --- mutators ---

    @Test
    void renameShouldUpdateName() {
        AlarmCondition condition = new AlarmCondition("up", ComparisonOperator.GT, "0");
        Alarm alarm = new Alarm("Old", "desc", condition);

        alarm.rename("New");
        assertEquals("New", alarm.getName());
    }

    @Test
    void renameShouldRejectBlank() {
        AlarmCondition condition = new AlarmCondition("up", ComparisonOperator.GT, "0");
        Alarm alarm = new Alarm("Test", "desc", condition);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> alarm.rename("  "));
        assertEquals("Name cannot be blank", ex.getMessage());
    }

    @Test
    void changeConditionShouldRejectNull() {
        AlarmCondition condition = new AlarmCondition("up", ComparisonOperator.GT, "0");
        Alarm alarm = new Alarm("Test", "desc", condition);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> alarm.changeCondition(null));
        assertEquals("Condition cannot be null", ex.getMessage());
    }
}
