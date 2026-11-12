package com.bankingApp.offersToClients.service.dynamic;

import com.bankingApp.offersToClients.model.ProductType;
import com.bankingApp.offersToClients.model.QueryType;
import com.bankingApp.offersToClients.repository.RecommendationsRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ActiveUserOfQueryEvaluator implements QueryEvaluator {

    private static final int ACTIVE_USER_MIN_TRANSACTIONS = 5;

    private final RecommendationsRepository recommendationsRepository;

    public ActiveUserOfQueryEvaluator(RecommendationsRepository recommendationsRepository) {
        this.recommendationsRepository = recommendationsRepository;
    }

    @Override
    public QueryType type() {
        return QueryType.ACTIVE_USER_OF;
    }

    @Override
    public boolean evaluate(UUID userId, List<String> arguments) {
        ProductType productType = ProductType.valueOf(arguments.get(0));
        return recommendationsRepository.countTransactions(userId, productType)
                >= ACTIVE_USER_MIN_TRANSACTIONS;
    }
}