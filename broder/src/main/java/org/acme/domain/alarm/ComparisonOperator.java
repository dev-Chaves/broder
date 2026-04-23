package org.acme.domain.alarm;

public enum ComparisonOperator {
    GT(">"),
    LT("<"),
    GTE(">="),
    LTE("<="),
    EQ("==");

    private final String symbol;

    ComparisonOperator(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    public static ComparisonOperator fromSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Comparison operator cannot be blank");
        }
        for (ComparisonOperator op : values()) {
            if (op.symbol.equals(symbol.trim())) {
                return op;
            }
        }
        throw new IllegalArgumentException("Invalid comparison operator: " + symbol + ". Must be one of: >, <, >=, <=, ==");
    }

    public boolean apply(double value, double threshold) {
        return switch (this) {
            case GT -> value > threshold;
            case LT -> value < threshold;
            case GTE -> value >= threshold;
            case LTE -> value <= threshold;
            case EQ -> Math.abs(value - threshold) < 1e-9;
        };
    }
}
