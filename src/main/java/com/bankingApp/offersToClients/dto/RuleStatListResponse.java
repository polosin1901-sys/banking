package com.bankingApp.offersToClients.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RuleStatListResponse(
        @JsonProperty("stats") List<RuleStatDto> stats
) {
}
