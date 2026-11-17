package com.bankingApp.offersToClients.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

/**
 * Stored dynamic rule returned by the rule endpoints.
 *
 * @param id          generated rule identifier
 * @param productName product name shown to the client
 * @param productId   product identifier
 * @param productText marketing description of the product
 * @param rule        rule queries
 */
public record RuleResponse(
        Long id,
        @JsonProperty("product_name") String productName,
        @JsonProperty("product_id") UUID productId,
        @JsonProperty("product_text") String productText,
        @JsonProperty("rule") List<QueryDto> rule
) {
}
