package com.bankingApp.offersToClients.rules;

import com.bankingApp.offersToClients.dto.RecommendationDto;

import java.util.Optional;
import java.util.UUID;

public interface RecommendationRuleSet {
    Optional<RecommendationDto> check(UUID userId);
}
