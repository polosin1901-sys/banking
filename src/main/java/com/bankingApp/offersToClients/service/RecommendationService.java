package com.bankingApp.offersToClients.service;

import com.bankingApp.offersToClients.dto.RecommendationDto;
import com.bankingApp.offersToClients.rules.RecommendationRuleSet;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RecommendationService {

    private final List<RecommendationRuleSet> ruleSets;

    public RecommendationService(List<RecommendationRuleSet> ruleSets) {
        this.ruleSets = ruleSets;
    }

    public List<RecommendationDto> getRecommendations(UUID userID) {
        return ruleSets.stream()
                .map(ruleSet -> ruleSet.check(userID))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}
