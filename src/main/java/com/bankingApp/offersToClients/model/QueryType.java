package com.bankingApp.offersToClients.model;

import java.util.Arrays;

/**
 * Query types supported by dynamic rules and their evaluators.
 */
public enum QueryType {
    /** Client has at least one transaction of the given product type. */
    USER_OF,
    /** Client has at least five transactions of the given product type. */
    ACTIVE_USER_OF,
    /** Transaction sum of a product and transaction type compares to a constant. */
    TRANSACTION_SUM_COMPARE,
    /** Deposit sum compares to withdraw sum of the given product type. */
    TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW;

    /**
     * @param value textual query type stored in the database
     * @return matching enum constant
     * @throws IllegalArgumentException when the value is unknown
     */
    public static QueryType fromString(String value) {
        return Arrays.stream(values())
                .filter(type -> type.name().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown query type: " + value
                ));
    }
}
