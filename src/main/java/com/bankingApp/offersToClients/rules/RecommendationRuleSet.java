package com.bankingApp.offersToClients.rules;

import com.bankingApp.offersToClients.dto.RecommendationDto;

import java.util.Optional;
import java.util.UUID;

/**
 * Static recommendation rule from the first task.
 *
 * <p>Implementations are discovered by Spring; {@code RecommendationService} evaluates all of them
 * on every request. Static rules are hard coded on purpose: the first task defines them as a fixed
 * part of the business logic, while the manager may add dynamic rules on top.</p>
 */
public interface RecommendationRuleSet {

    /**
     * @param userId client identifier
     * @return recommended product when the rule matches, otherwise {@link Optional#empty()}
     */
    Optional<RecommendationDto> check(UUID userId);
}
