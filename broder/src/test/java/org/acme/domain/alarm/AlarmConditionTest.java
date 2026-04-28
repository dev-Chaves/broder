package org.acme.domain.alarm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlarmConditionTest {

    @Test
    void shouldCreateValidCondition() {
        AlarmCondition condition = new AlarmCondition("up > 0", ComparisonOperator.GT, "0");

        assertEquals("up > 0", condition.query());
        assertEquals(ComparisonOperator.GT, condition.operator());
        assertEquals("0", condition.threshold());
        assertEquals(0.0, condition.thresholdAsDouble(), 1e-9);
    }

    @Test
    void shouldTrimQuery() {
        AlarmCondition condition = new AlarmCondition("  up  ", ComparisonOperator.GT, "1");
        assertEquals("up", condition.query());
    }

    @Test
    void shouldRejectNullQuery() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new AlarmCondition(null, ComparisonOperator.GT, "1"));
        assertEquals("Query cannot be blank", ex.getMessage());
    }

    @Test
    void shouldRejectBlankQuery() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new AlarmCondition("   ", ComparisonOperator.GT, "1"));
        assertEquals("Query cannot be blank", ex.getMessage());
    }

    @Test
    void shouldRejectNullOperator() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new AlarmCondition("up", null, "1"));
        assertEquals("Comparison operator cannot be null", ex.getMessage());
    }

    @Test
    void shouldRejectNullThreshold() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new AlarmCondition("up", ComparisonOperator.GT, null));
        assertEquals("Threshold cannot be blank", ex.getMessage());
    }

    @Test
    void shouldRejectBlankThreshold() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new AlarmCondition("up", ComparisonOperator.GT, "   "));
        assertEquals("Threshold cannot be blank", ex.getMessage());
    }

    @Test
    void shouldRejectInvalidThreshold() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new AlarmCondition("up", ComparisonOperator.GT, "abc"));
        assertEquals("Threshold must be a valid number: abc", ex.getMessage());
    }

    @Test
    void shouldAcceptNegativeThreshold() {
        AlarmCondition condition = new AlarmCondition("up", ComparisonOperator.GT, "-5.5");
        assertEquals(-5.5, condition.thresholdAsDouble(), 1e-9);
    }

    @Test
    void matchesShouldDelegateToOperator() {
        AlarmCondition condition = new AlarmCondition("up", ComparisonOperator.GT, "0");
        assertTrue(condition.matches(1.0));
        assertFalse(condition.matches(-1.0));
    }

    @Test
    void toStringShouldFormatNicely() {
        AlarmCondition condition = new AlarmCondition("up", ComparisonOperator.GT, "0");
        assertEquals("up > 0", condition.toString());
    }
}
