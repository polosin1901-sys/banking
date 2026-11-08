package com.bankingApp.offersToClients.service;

import com.bankingApp.offersToClients.dto.RecommendationDto;
import com.bankingApp.offersToClients.rules.RecommendationRuleSet;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class RecommendationService {

    private final List<RecommendationRuleSet> ruleSets;
    private final DynamicRuleService dynamicRuleService;

    public RecommendationService(List<RecommendationRuleSet> ruleSets,
                                 DynamicRuleService dynamicRuleService) {
        this.ruleSets = ruleSets;
        this.dynamicRuleService = dynamicRuleService;
    }

    public List<RecommendationDto> getRecommendations(UUID userID) {
        Map<UUID, RecommendationDto> recommendations = new LinkedHashMap<>();

        ruleSets.stream()
                .map(ruleSet -> ruleSet.check(userID))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(recommendation ->
                        recommendations.putIfAbsent(recommendation.id(), recommendation)
                );

        dynamicRuleService.getRecommendations(userID)
                .forEach(recommendation ->
                        recommendations.putIfAbsent(recommendation.id(), recommendation)
                );

        return List.copyOf(recommendations.values());
    }
}