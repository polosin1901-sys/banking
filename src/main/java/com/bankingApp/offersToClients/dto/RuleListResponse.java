package com.bankingApp.offersToClients.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RuleListResponse(
        @JsonProperty("data") List<RuleResponse> data
) {
}
