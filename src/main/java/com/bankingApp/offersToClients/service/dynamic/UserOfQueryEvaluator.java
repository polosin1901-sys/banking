package com.bankingApp.offersToClients.service.dynamic;

import com.bankingApp.offersToClients.model.ProductType;
import com.bankingApp.offersToClients.model.QueryType;
import com.bankingApp.offersToClients.repository.RecommendationsRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class UserOfQueryEvaluator implements QueryEvaluator {

    private final RecommendationsRepository recommendationsRepository;

    public UserOfQueryEvaluator(RecommendationsRepository recommendationsRepository) {
        this.recommendationsRepository = recommendationsRepository;
    }

    @Override
    public QueryType type() {
        return QueryType.USER_OF;
    }

    @Override
    public boolean evaluate(UUID userId, List<String> arguments) {
        ProductType productType = ProductType.valueOf(arguments.get(0));
        return recommendationsRepository.countTransactions(userId, productType) >= 1;
    }
}
