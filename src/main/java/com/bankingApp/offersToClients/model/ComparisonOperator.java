package com.bankingApp.offersToClients.model;

import java.util.Arrays;

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

    public abstract boolean test(int left, int right);

    public String symbol() {
        return symbol;
    }

    public static ComparisonOperator fromString(String value) {
        return Arrays.stream(values())
                .filter(operator -> operator.symbol.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported comparison operator: " + value
                ));
    }
}
