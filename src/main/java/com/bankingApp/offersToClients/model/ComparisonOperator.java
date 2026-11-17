package com.bankingApp.offersToClients.model;

import java.util.Arrays;

/**
 * Comparison operators allowed in dynamic rules.
 *
 * <p>Each constant knows both its textual form and how to compare two integers, so the
 * {@code TRANSACTION_SUM_COMPARE} evaluators do not need a switch statement.</p>
 */
public enum ComparisonOperator {
    GT(">") {
        @Override
        public boolean test(int left, int right) {
            return left > right;
        }
    },
    LT("<") {
        @Override
        public boolean test(int left, int right) {
            return left < right;
        }
    },
    EQ("=") {
        @Override
        public boolean test(int left, int right) {
            return left == right;
        }
    },
    GE(">=") {
        @Override
        public boolean test(int left, int right) {
            return left >= right;
        }
    },
    LE("<=") {
        @Override
        public boolean test(int left, int right) {
            return left <= right;
        }
    };

    private final String symbol;

    ComparisonOperator(String symbol) {
        this.symbol = symbol;
    }

    /**
     * @param left  left operand
     * @param right right operand
     * @return result of applying this operator to the operands
     */
    public abstract boolean test(int left, int right);

    /**
     * @return textual form of the operator, for example {@code >=}
     */
    public String symbol() {
        return symbol;
    }

    /**
     * @param value textual operator, for example {@code >=}
     * @return matching enum constant
     * @throws IllegalArgumentException when the operator is not supported
     */
    public static ComparisonOperator fromString(String value) {
        return Arrays.stream(values())
                .filter(operator -> operator.symbol.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported comparison operator: " + value
                ));
    }
}
