package org.acme.domain.alarm;

import org.acme.domain.alarm.enums.ComparisonOperator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComparisonOperatorTest {

    // --- fromSymbol ---

    @Test
    void fromSymbolShouldParseAllValidOperators() {
        assertEquals(ComparisonOperator.GT, ComparisonOperator.fromSymbol(">"));
        assertEquals(ComparisonOperator.LT, ComparisonOperator.fromSymbol("<"));
        assertEquals(ComparisonOperator.GTE, ComparisonOperator.fromSymbol(">="));
        assertEquals(ComparisonOperator.LTE, ComparisonOperator.fromSymbol("<="));
        assertEquals(ComparisonOperator.EQ, ComparisonOperator.fromSymbol("=="));
    }

    @Test
    void fromSymbolShouldTrimWhitespace() {
        assertEquals(ComparisonOperator.GT, ComparisonOperator.fromSymbol(" > "));
    }

    @Test
    void fromSymbolShouldRejectNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ComparisonOperator.fromSymbol(null));
        assertEquals("Comparison operator cannot be blank", ex.getMessage());
    }

    @Test
    void fromSymbolShouldRejectBlank() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ComparisonOperator.fromSymbol("   "));
        assertEquals("Comparison operator cannot be blank", ex.getMessage());
    }

    @Test
    void fromSymbolShouldRejectInvalidSymbol() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ComparisonOperator.fromSymbol("!="));
        assertTrue(ex.getMessage().contains("Invalid comparison operator"));
    }

    // --- apply ---

    @Test
    void gtShouldReturnTrueWhenValueGreaterThanThreshold() {
        assertTrue(ComparisonOperator.GT.apply(5.0, 3.0));
    }

    @Test
    void gtShouldReturnFalseWhenValueEqualOrLess() {
        assertFalse(ComparisonOperator.GT.apply(3.0, 3.0));
        assertFalse(ComparisonOperator.GT.apply(2.0, 3.0));
    }

    @Test
    void ltShouldReturnTrueWhenValueLessThanThreshold() {
        assertTrue(ComparisonOperator.LT.apply(2.0, 5.0));
    }

    @Test
    void gteShouldReturnTrueWhenValueGreaterOrEqual() {
        assertTrue(ComparisonOperator.GTE.apply(5.0, 3.0));
        assertTrue(ComparisonOperator.GTE.apply(3.0, 3.0));
    }

    @Test
    void lteShouldReturnTrueWhenValueLessOrEqual() {
        assertTrue(ComparisonOperator.LTE.apply(2.0, 5.0));
        assertTrue(ComparisonOperator.LTE.apply(5.0, 5.0));
    }

    @Test
    void eqShouldReturnTrueWithinTolerance() {
        assertTrue(ComparisonOperator.EQ.apply(1.0, 1.0));
        assertTrue(ComparisonOperator.EQ.apply(1.0000000001, 1.0));
    }

    @Test
    void eqShouldReturnFalseOutsideTolerance() {
        assertFalse(ComparisonOperator.EQ.apply(1.1, 1.0));
    }

    @Test
    void symbolShouldReturnCorrectString() {
        assertEquals(">", ComparisonOperator.GT.symbol());
        assertEquals("<", ComparisonOperator.LT.symbol());
        assertEquals(">=", ComparisonOperator.GTE.symbol());
        assertEquals("<=", ComparisonOperator.LTE.symbol());
        assertEquals("==", ComparisonOperator.EQ.symbol());
    }
}
