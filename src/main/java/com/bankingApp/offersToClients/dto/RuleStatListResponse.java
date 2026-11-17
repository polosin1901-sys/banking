package com.bankingApp.offersToClients.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response of {@code GET /rule/stats}.
 *
 * @param stats hit counters for all rules, including rules with zero hits
 */
public record RuleStatListResponse(
        @JsonProperty("stats") List<RuleStatDto> stats
) {
}
