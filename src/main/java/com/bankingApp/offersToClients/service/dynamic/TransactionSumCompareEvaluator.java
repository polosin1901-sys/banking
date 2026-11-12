package com.bankingApp.offersToClients.service.dynamic;

import com.bankingApp.offersToClients.model.ComparisonOperator;
import com.bankingApp.offersToClients.model.ProductType;
import com.bankingApp.offersToClients.model.QueryType;
import com.bankingApp.offersToClients.model.TransactionType;
import com.bankingApp.offersToClients.repository.RecommendationsRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class TransactionSumCompareEvaluator implements QueryEvaluator {

    private final RecommendationsRepository recommendationsRepository;

    public TransactionSumCompareEvaluator(RecommendationsRepository recommendationsRepository) {
        this.recommendationsRepository = recommendationsRepository;
    }

    @Override
    public QueryType type() {
        return QueryType.TRANSACTION_SUM_COMPARE;
    }

    @Override
    public boolean evaluate(UUID userId, List<String> arguments) {
        ProductType productType = ProductType.valueOf(arguments.get(0));
        TransactionType transactionType = TransactionType.valueOf(arguments.get(1));
        ComparisonOperator operator = ComparisonOperator.fromString(arguments.get(2));
        int constant = Integer.parseInt(arguments.get(3));

        int sum = recommendationsRepository.sumTransactions(
                userId,
                productType,
                transactionType
        );

        return operator.test(sum, constant);
    }
}