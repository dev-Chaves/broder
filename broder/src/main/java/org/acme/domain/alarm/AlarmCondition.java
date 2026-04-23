package org.acme.domain.alarm;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class AlarmCondition {

    @Column(nullable = false)
    private String query;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "comparison")
    private ComparisonOperator operator;

    @Column(nullable = false, name = "threshold")
    private String threshold;

    protected AlarmCondition() {
        // JPA
    }

    public AlarmCondition(String query, ComparisonOperator operator, String threshold) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be blank");
        }
        if (operator == null) {
            throw new IllegalArgumentException("Comparison operator cannot be null");
        }
        if (threshold == null || threshold.isBlank()) {
            throw new IllegalArgumentException("Threshold cannot be blank");
        }
        try {
            Double.parseDouble(threshold);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Threshold must be a valid number: " + threshold);
        }
        this.query = query.trim();
        this.operator = operator;
        this.threshold = threshold;
    }

    public String query() {
        return query;
    }

    public ComparisonOperator operator() {
        return operator;
    }

    public String threshold() {
        return threshold;
    }

    public double thresholdAsDouble() {
        return Double.parseDouble(threshold);
    }

    public boolean matches(double value) {
        return operator.apply(value, thresholdAsDouble());
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", query, operator.symbol(), threshold);
    }
}
