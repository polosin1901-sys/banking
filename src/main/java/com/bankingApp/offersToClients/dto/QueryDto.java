package com.bankingApp.offersToClients.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * One query of a dynamic rule.
 *
 * @param query     query type, one of {@code USER_OF}, {@code ACTIVE_USER_OF},
 *                  {@code TRANSACTION_SUM_COMPARE}, {@code TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW}
 * @param arguments query arguments in the order expected by the query type
 * @param negate    when {@code true} the query result is inverted
 */
public record QueryDto(
        @JsonProperty("query") String query,
        @JsonProperty("arguments") List<String> arguments,
        @JsonProperty("negate") boolean negate
) {
}
