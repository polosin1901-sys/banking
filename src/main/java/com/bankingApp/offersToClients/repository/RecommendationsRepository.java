package com.bankingApp.offersToClients.repository;

import com.bankingApp.offersToClients.cache.RecommendationsCache;
import com.bankingApp.offersToClients.model.ProductType;
import com.bankingApp.offersToClients.model.TransactionSums;
import com.bankingApp.offersToClients.model.TransactionType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class RecommendationsRepository {

    private static final String COUNT_TRANSACTIONS_SQL = """
            SELECT COUNT(*)
            FROM transactions t
            JOIN products p ON t.product_id = p.id
            WHERE t.user_id = ? AND p.type = ?
            """;

    private static final String SUM_TRANSACTIONS_SQL = """
            SELECT COALESCE(SUM(t.amount), 0)
            FROM transactions t
            JOIN products p ON t.product_id = p.id
            WHERE t.user_id = ? AND p.type = ? AND t.type = ?
            """;

    private static final String SUM_DEPOSIT_AND_WITHDRAW_SQL = """
            SELECT
                COALESCE(SUM(CASE WHEN t.type = 'DEPOSIT' THEN t.amount ELSE 0 END), 0) AS deposit_sum,
                COALESCE(SUM(CASE WHEN t.type = 'WITHDRAW' THEN t.amount ELSE 0 END), 0) AS withdraw_sum
            FROM transactions t
            JOIN products p ON t.product_id = p.id
            WHERE t.user_id = ? AND p.type = ?
            """;

    private final JdbcTemplate jdbcTemplate;
    private final RecommendationsCache cache;

    public RecommendationsRepository(
            @Qualifier("recommendationsJdbcTemplate") JdbcTemplate jdbcTemplate,
            RecommendationsCache cache) {
        this.jdbcTemplate = jdbcTemplate;
        this.cache = cache;
    }

    public int countTransactions(UUID userId, ProductType productType) {
        return cache.count(userId, productType, () -> {
            Integer count = jdbcTemplate.queryForObject(
                    COUNT_TRANSACTIONS_SQL,
                    Integer.class,
                    userId,
                    productType.name()
            );
            return count != null ? count : 0;
        });
    }

    public int sumTransactions(UUID userId,
                               ProductType productType,
                               TransactionType transactionType) {
        return cache.sum(userId, productType, transactionType, () -> {
            Integer sum = jdbcTemplate.queryForObject(
                    SUM_TRANSACTIONS_SQL,
                    Integer.class,
                    userId,
                    productType.name(),
                    transactionType.name()
            );
            return sum != null ? sum : 0;
        });
    }

    public TransactionSums sumDepositAndWithdraw(UUID userId, ProductType productType) {
        return cache.depositAndWithdraw(userId, productType, () ->
                jdbcTemplate.queryForObject(
                        SUM_DEPOSIT_AND_WITHDRAW_SQL,
                        (rs, rowNum) -> new TransactionSums(
                                rs.getInt("deposit_sum"),
                                rs.getInt("withdraw_sum")
                        ),
                        userId,
                        productType.name()
                )
        );
    }
}