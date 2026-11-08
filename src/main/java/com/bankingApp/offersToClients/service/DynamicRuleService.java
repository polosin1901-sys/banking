package com.bankingApp.offersToClients.service;

import com.bankingApp.offersToClients.dto.RecommendationDto;
import com.bankingApp.offersToClients.entity.QueryEntity;
import com.bankingApp.offersToClients.entity.RuleEntity;
import com.bankingApp.offersToClients.model.ProductType;
import com.bankingApp.offersToClients.model.QueryType;
import com.bankingApp.offersToClients.model.TransactionSums;
import com.bankingApp.offersToClients.model.TransactionType;
import com.bankingApp.offersToClients.repository.RecommendationsRepository;
import com.bankingApp.offersToClients.repository.RuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DynamicRuleService {

    private static final int ACTIVE_USER_MIN_TRANSACTIONS = 5;

    private final RuleRepository ruleRepository;
    private final RecommendationsRepository recommendationsRepository;

    public DynamicRuleService(RuleRepository ruleRepository,
                              RecommendationsRepository recommendationsRepository) {
        this.ruleRepository = ruleRepository;
        this.recommendationsRepository = recommendationsRepository;
    }

    @Transactional(readOnly = true)
    public List<RecommendationDto> getRecommendations(UUID userId) {
        return ruleRepository.findAllWithQueries().stream()
                .filter(rule -> matches(userId, rule))
                .map(rule -> new RecommendationDto(
                        rule.getProductName(),
                        rule.getProductId(),
                        rule.getProductText()
                ))
                .toList();
    }

    private boolean matches(UUID userId, RuleEntity rule) {
        return !rule.getRule().isEmpty()
                && rule.getRule().stream().allMatch(query -> matches(userId, query));
    }

    private boolean matches(UUID userId, QueryEntity query) {
        boolean result = switch (QueryType.fromString(query.getQuery())) {
            case USER_OF -> matchesUserOf(userId, query, 1);
            case ACTIVE_USER_OF -> matchesUserOf(userId, query, ACTIVE_USER_MIN_TRANSACTIONS);
            case TRANSACTION_SUM_COMPARE -> matchesTransactionSum(userId, query);
            case TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW -> matchesDepositWithdraw(userId, query);
        };

        return query.isNegate() ? !result : result;
    }

    private boolean matchesUserOf(UUID userId, QueryEntity query, int minTransactions) {
        ProductType productType = ProductType.valueOf(query.getArguments().get(0));
        int count = recommendationsRepository.countTransactions(userId, productType);
        return count >= minTransactions;
    }

    private boolean matchesTransactionSum(UUID userId, QueryEntity query) {
        List<String> arguments = query.getArguments();

        ProductType productType = ProductType.valueOf(arguments.get(0));
        TransactionType transactionType = TransactionType.valueOf(arguments.get(1));
        String operator = arguments.get(2);
        int constant = Integer.parseInt(arguments.get(3));

        int sum = recommendationsRepository.sumTransactions(
                userId,
                productType,
                transactionType
        );

        return compare(sum, operator, constant);
    }

    private boolean matchesDepositWithdraw(UUID userId, QueryEntity query) {
        List<String> arguments = query.getArguments();

        ProductType productType = ProductType.valueOf(arguments.get(0));
        String operator = arguments.get(1);

        TransactionSums sums = recommendationsRepository.sumDepositAndWithdraw(
                userId,
                productType
        );

        return compare(sums.deposit(), operator, sums.withdraw());
    }

    private boolean compare(int left, String operator, int right) {
        return switch (operator) {
            case ">" -> left > right;
            case "<" -> left < right;
            case "=" -> left == right;
            case ">=" -> left >= right;
            case "<=" -> left <= right;
            default -> throw new IllegalArgumentException(
                    "Unsupported comparison operator: " + operator
            );
        };
    }
}