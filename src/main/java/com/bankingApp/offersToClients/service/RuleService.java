package com.bankingApp.offersToClients.service;

import com.bankingApp.offersToClients.dto.QueryDto;
import com.bankingApp.offersToClients.dto.RuleRequest;
import com.bankingApp.offersToClients.dto.RuleResponse;
import com.bankingApp.offersToClients.entity.QueryEntity;
import com.bankingApp.offersToClients.entity.RuleEntity;
import com.bankingApp.offersToClients.model.QueryType;
import com.bankingApp.offersToClients.model.ProductType;
import com.bankingApp.offersToClients.model.TransactionType;
import com.bankingApp.offersToClients.repository.RuleRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class RuleService {

    private static final Set<String> COMPARISON_OPERATORS =
            Set.of(">", "<", "=", ">=", "<=");

    private final RuleRepository ruleRepository;

    public RuleService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Transactional
    public RuleResponse createRule(RuleRequest request) {
        validate(request);
        RuleEntity rule = ruleRepository.save(toEntity(request));
        return toResponse(rule);
    }

    @Transactional
    public List<RuleResponse> getAllRules() {
        return ruleRepository.findAllWithQueries().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public boolean deleteRule(Long id) {
        if (id == null || !ruleRepository.existsById(id)) {
            return false;
        }
        ruleRepository.deleteById(id);
        return true;
    }

    private RuleEntity toEntity(RuleRequest request) {
        List<QueryEntity> queries = request.rule().stream()
                .map(query -> new QueryEntity(
                        query.query(),
                        query.arguments(),
                        query.negate()
                ))
                .toList();

        return new RuleEntity(
                request.productName(),
                request.productId(),
                request.productText(),
                queries
        );
    }

    private RuleResponse toResponse(RuleEntity rule) {
        List<QueryDto> queries = rule.getRule().stream()
                .map(query -> new QueryDto(
                        query.getQuery(),
                        List.copyOf(query.getArguments()),
                        query.isNegate()
                ))
                .toList();

        return new RuleResponse(
                rule.getId(),
                rule.getProductName(),
                rule.getProductId(),
                rule.getProductText(),
                queries
        );
    }

    private void validate(RuleRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body must not be null");
        }

        requireText(request.productName(), "product_name");

        if (request.productId() == null) {
            throw new IllegalArgumentException("product_id must not be null");
        }

        requireText(request.productText(), "product_text");

        if (request.rule() == null || request.rule().isEmpty()) {
            throw new IllegalArgumentException("rule must contain at least one query");
        }

        request.rule().forEach(this::validateQuery);
    }

    private void validateQuery(QueryDto query) {
        if (query == null) {
            throw new IllegalArgumentException("rule must not contain null queries");
        }

        requireText(query.query(), "query");

        if (query.arguments() == null) {
            throw new IllegalArgumentException(
                    "Query " + query.query() + ": arguments must not be null"
            );
        }

        if (query.arguments().stream()
                .anyMatch(argument -> argument == null || argument.isBlank())) {
            throw new IllegalArgumentException(
                    "Query " + query.query() + ": arguments must not contain blank values"
            );
        }
        QueryType queryType = QueryType.fromString(query.query());
        switch (queryType) {
            case USER_OF, ACTIVE_USER_OF -> {
                requireArgumentCount(query, 1);
                requireProductType(query.arguments().get(0), query.query());
            }
            case TRANSACTION_SUM_COMPARE -> {
                requireArgumentCount(query, 4);
                requireProductType(query.arguments().get(0), query.query());
                requireTransactionType(query.arguments().get(1), query.query());
                requireOperator(query.arguments().get(2), query.query());
                requireNonNegativeInteger(query.arguments().get(3), query.query());
            }
            case TRANSACTION_SUM_COMPARE_DEPOSIT_WITHDRAW -> {
                requireArgumentCount(query, 2);
                requireProductType(query.arguments().get(0), query.query());
                requireOperator(query.arguments().get(1), query.query());
            }
        }
    }

    private void requireArgumentCount(QueryDto query, int expected) {
        if (query.arguments().size() != expected) {
            throw new IllegalArgumentException(
                    "Query " + query.query() + " expects " + expected
                            + " argument(s), but got " + query.arguments().size()
            );
        }
    }

    private void requireProductType(String value, String query) {
        try {
            ProductType.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Query " + query + ": unknown product type '" + value + "'"
            );
        }
    }

    private void requireTransactionType(String value, String query) {
        try {
            TransactionType.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Query " + query + ": unknown transaction type '" + value + "'"
            );
        }
    }

    private void requireOperator(String value, String query) {
        if (!COMPARISON_OPERATORS.contains(value)) {
            throw new IllegalArgumentException(
                    "Query " + query + ": unsupported operator '" + value + "'"
            );
        }
    }

    private void requireNonNegativeInteger(String value, String query) {
        int number;
        try {
            number = Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Query " + query + ": argument '" + value + "' must be an integer"
            );
        }

        if (number < 0) {
            throw new IllegalArgumentException(
                    "Query " + query + ": argument '" + value + "' must be non-negative"
            );
        }
    }

    private void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
