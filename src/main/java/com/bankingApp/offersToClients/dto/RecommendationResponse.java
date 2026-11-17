package com.bankingApp.offersToClients.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

/**
 * Response of {@code GET /recommendation/{user_id}}.
 *
 * @param user_id         requested client identifier
 * @param recommendations products recommended for the client
 */
public record RecommendationResponse(
        @JsonProperty("user_id") UUID user_id,
        List<RecommendationDto> recommendations) {
}
