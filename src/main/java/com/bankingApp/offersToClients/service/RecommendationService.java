package com.bankingApp.offersToClients.service;

import com.bankingApp.offersToClients.dto.RecommendationDto;
import com.bankingApp.offersToClients.rules.RecommendationRuleSet;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Aggregates recommendations from static rule sets and dynamic rules.
 *
 * <p>Static rule sets come from the first task (Invest500, TopSaving, SimpleCredit) and are hard
 * coded as {@link RecommendationRuleSet} implementations. Dynamic rules are evaluated by
 * {@link DynamicRuleService}. Results are de-duplicated by product identifier: a static rule wins
 * when the same product is also produced by a dynamic rule.</p>
 */
@Service
public class RecommendationService {

    private final List<RecommendationRuleSet> ruleSets;
    private final DynamicRuleService dynamicRuleService;

    public RecommendationService(List<RecommendationRuleSet> ruleSets,
                                 DynamicRuleService dynamicRuleService) {
        this.ruleSets = ruleSets;
        this.dynamicRuleService = dynamicRuleService;
    }

    /**
     * Builds the final recommendation list for a client.
     *
     * @param userId client identifier
     * @return unique recommendations; static rules first, dynamic rules afterwards
     */
    public List<RecommendationDto> getRecommendations(UUID userId) {
        Map<UUID, RecommendationDto> recommendations = new LinkedHashMap<>();

        ruleSets.stream()
                .map(ruleSet -> ruleSet.check(userId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(recommendation ->
                        recommendations.putIfAbsent(recommendation.id(), recommendation)
                );

        dynamicRuleService.getRecommendations(userId)
                .forEach(recommendation ->
                        recommendations.putIfAbsent(recommendation.id(), recommendation)
                );

        return List.copyOf(recommendations.values());
    }
}
