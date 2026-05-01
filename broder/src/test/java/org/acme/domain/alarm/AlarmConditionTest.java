package org.acme.domain.alarm;

import org.acme.domain.alarm.enums.ComparisonOperator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlarmConditionTest {

    @Test
    void shouldCreateValidCondition() {
        AlarmCondition condition = AlarmCondition.builder()
                .query("up > 0")
                .operator(ComparisonOperator.GT)
                .threshold("0")
                .build();

        assertEquals("up > 0", condition.query());
        assertEquals(ComparisonOperator.GT, condition.operator());
        assertEquals("0", condition.threshold());
        assertEquals(0.0, condition.thresholdAsDouble(), 1e-9);
    }

    @Test
    void shouldTrimQuery() {
        AlarmCondition condition = AlarmCondition.builder()
                .query("  up  ")
                .operator(ComparisonOperator.GT)
                .threshold("1")
                .build();
        assertEquals("up", condition.query());
    }

    @Test
    void shouldRejectNullQuery() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AlarmCondition.builder()
                        .query(null)
                        .operator(ComparisonOperator.GT)
                        .threshold("1")
                        .build());
        assertEquals("Query cannot be blank", ex.getMessage());
    }

    @Test
    void shouldRejectBlankQuery() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AlarmCondition.builder()
                        .query("   ")
                        .operator(ComparisonOperator.GT)
                        .threshold("1")
                        .build());
        assertEquals("Query cannot be blank", ex.getMessage());
    }

    @Test
    void shouldRejectNullOperator() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AlarmCondition.builder()
                        .query("up")
                        .operator(null)
                        .threshold("1")
                        .build());
        assertEquals("Comparison operator cannot be null", ex.getMessage());
    }

    @Test
    void shouldRejectNullThreshold() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AlarmCondition.builder()
                        .query("up")
                        .operator(ComparisonOperator.GT)
                        .threshold(null)
                        .build());
        assertEquals("Threshold cannot be blank", ex.getMessage());
    }

    @Test
    void shouldRejectBlankThreshold() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AlarmCondition.builder()
                        .query("up")
                        .operator(ComparisonOperator.GT)
                        .threshold("   ")
                        .build());
        assertEquals("Threshold cannot be blank", ex.getMessage());
    }

    @Test
    void shouldRejectInvalidThreshold() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AlarmCondition.builder()
                        .query("up")
                        .operator(ComparisonOperator.GT)
                        .threshold("abc")
                        .build());
        assertEquals("Threshold must be a valid number: abc", ex.getMessage());
    }

    @Test
    void shouldAcceptNegativeThreshold() {
        AlarmCondition condition = AlarmCondition.builder()
                .query("up")
                .operator(ComparisonOperator.GT)
                .threshold("-5.5")
                .build();
        assertEquals(-5.5, condition.thresholdAsDouble(), 1e-9);
    }

    @Test
    void matchesShouldDelegateToOperator() {
        AlarmCondition condition = AlarmCondition.builder()
                .query("up")
                .operator(ComparisonOperator.GT)
                .threshold("0")
                .build();
        assertTrue(condition.matches(1.0));
        assertFalse(condition.matches(-1.0));
    }

    @Test
    void toStringShouldFormatNicely() {
        AlarmCondition condition = AlarmCondition.builder()
                .query("up")
                .operator(ComparisonOperator.GT)
                .threshold("0")
                .build();
        assertEquals("up > 0", condition.toString());
    }
}
