package com.bankingApp.offersToClients.service;

import com.bankingApp.offersToClients.dto.RecommendationDto;
import com.bankingApp.offersToClients.entity.QueryEntity;
import com.bankingApp.offersToClients.entity.RuleEntity;
import com.bankingApp.offersToClients.model.QueryType;
import com.bankingApp.offersToClients.repository.RuleRepository;
import com.bankingApp.offersToClients.service.dynamic.QueryEvaluator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Evaluates dynamic rules created by the manager.
 *
 * <p>A rule matches when all of its queries match; every query is delegated to the
 * {@link QueryEvaluator} registered for its {@link QueryType}. Successful matches increment the
 * rule hit counter, therefore the method runs in a read-write transaction.</p>
 */
@Service
public class DynamicRuleService {

    private final RuleRepository ruleRepository;
    private final Map<QueryType, QueryEvaluator> evaluators;
    private final RuleStatService ruleStatService;

    public DynamicRuleService(RuleRepository ruleRepository,
                              RuleStatService ruleStatService,
                              List<QueryEvaluator> evaluators) {
        this.ruleRepository = ruleRepository;
        this.ruleStatService = ruleStatService;
        // One evaluator per query type: the list injected by Spring becomes a lookup table.
        this.evaluators = evaluators.stream()
                .collect(Collectors.toUnmodifiableMap(
                        QueryEvaluator::type,
                        Function.identity()
                ));
    }

    /**
     * Returns products whose dynamic rules match the client.
     *
     * @param userId client identifier
     * @return one recommendation per matched rule, in rule insertion order
     * @throws IllegalStateException when a stored rule references an unsupported query type
     */
    @Transactional
    public List<RecommendationDto> getRecommendations(UUID userId) {
        List<RecommendationDto> recommendations = new ArrayList<>();
        for (RuleEntity rule : ruleRepository.findAllWithQueries()) {
            if (!matches(userId, rule)) {
                continue;
            }

            ruleStatService.registerHit(rule.getId());

            recommendations.add(new RecommendationDto(
                    rule.getProductName(),
                    rule.getProductId(),
                    rule.getProductText()
            ));
        }
        return List.copyOf(recommendations);
    }

    private boolean matches(UUID userId, RuleEntity rule) {
        return !rule.getRule().isEmpty()
                && rule.getRule().stream().allMatch(query -> matches(userId, query));
    }

    private boolean matches(UUID userId, QueryEntity query) {
        QueryType queryType = QueryType.fromString(query.getQuery());

        QueryEvaluator evaluator = evaluators.get(queryType);
        if (evaluator == null) {
            throw new IllegalStateException(
                    "No evaluator registered for query type: " + queryType
            );
        }

        boolean result = evaluator.evaluate(userId, query.getArguments());
        return query.isNegate() ? !result : result;
    }
}
