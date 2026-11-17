package com.bankingApp.offersToClients.service.dynamic;

import com.bankingApp.offersToClients.model.QueryType;

import java.util.List;
import java.util.UUID;

/**
 * Evaluates a single query of a dynamic rule.
 *
 * <p>Every implementation reports the {@link QueryType} it supports; {@code DynamicRuleService}
 * collects all beans into a lookup table instead of using a switch statement.</p>
 */
public interface QueryEvaluator {

    /**
     * @return query type handled by this evaluator
     */
    QueryType type();

    /**
     * Evaluates the query for a client.
     *
     * @param userId    client identifier
     * @param arguments query arguments in the order defined by the query type
     * @return {@code true} when the query condition holds
     */
    boolean evaluate(UUID userId, List<String> arguments);
}
