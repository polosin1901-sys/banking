package com.bankingApp.offersToClients.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

/**
 * Request body of {@code POST /rule}.
 *
 * @param productName product name shown to the client
 * @param productId   product identifier
 * @param productText marketing description of the product
 * @param rule        queries that must all match for the rule to fire
 */
public record RuleRequest(
        @JsonProperty("product_name") String productName,
        @JsonProperty("product_id") UUID productId,
        @JsonProperty("product_text") String productText,
        @JsonProperty("rule") List<QueryDto> rule
) {
}
