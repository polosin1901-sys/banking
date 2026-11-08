package com.bankingApp.offersToClients.model;

import java.util.Arrays;

public enum QueryType {
    USER_OF,
    ACTIVE_USER_OF,
    TRANSACTION_SUM_COMPARE,
    TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW;

    public static QueryType fromString(String value) {
        return Arrays.stream(values())
                .filter(type -> type.name().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown query type: " + value
                ));
    }
}