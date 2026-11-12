package com.bankingApp.offersToClients.service.dynamic;

import com.bankingApp.offersToClients.model.ComparisonOperator;
import com.bankingApp.offersToClients.model.ProductType;
import com.bankingApp.offersToClients.model.QueryType;
import com.bankingApp.offersToClients.model.TransactionSums;
import com.bankingApp.offersToClients.repository.RecommendationsRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class TransactionSumDepositWithdrawCompareEvaluator implements QueryEvaluator {

    private final RecommendationsRepository recommendationsRepository;

    public TransactionSumDepositWithdrawCompareEvaluator(
            RecommendationsRepository recommendationsRepository) {
        this.recommendationsRepository = recommendationsRepository;
    }

    @Override
    public QueryType type() {
        return QueryType.TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW;
    }

    @Override
    public boolean evaluate(UUID userId, List<String> arguments) {
        ProductType productType = ProductType.valueOf(arguments.get(0));
        ComparisonOperator operator = ComparisonOperator.fromString(arguments.get(1));

        TransactionSums sums = recommendationsRepository.sumDepositAndWithdraw(
                userId,
                productType
        );

        return operator.test(sums.deposit(), sums.withdraw());
    }
}