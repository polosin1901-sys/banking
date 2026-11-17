package com.bankingApp.offersToClients.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Hit counter of a single dynamic rule.
 *
 * @param ruleId rule identifier
 * @param count  number of successful rule matches
 */
public record RuleStatDto(
        @JsonProperty("rule_id") Long ruleId,
        @JsonProperty("count") long count) {
}
