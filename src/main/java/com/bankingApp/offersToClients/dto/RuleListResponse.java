package com.bankingApp.offersToClients.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response of {@code GET /rule}.
 *
 * @param data all dynamic rules
 */
public record RuleListResponse(
        @JsonProperty("data") List<RuleResponse> data
) {
}
