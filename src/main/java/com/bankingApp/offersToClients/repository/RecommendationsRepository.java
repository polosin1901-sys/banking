package com.bankingApp.offersToClients.repository;

import com.bankingApp.offersToClients.model.ProductType;
import com.bankingApp.offersToClients.model.TransactionSums;
import com.bankingApp.offersToClients.model.TransactionType;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class RecommendationsRepository {

    private static final long MAX_CACHE_SIZE = 10_000;

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

    private final Cache<CountKey, Integer> countCache = Caffeine.newBuilder()
            .maximumSize(MAX_CACHE_SIZE)
            .build();

    private final Cache<SumKey, Integer> sumCache = Caffeine.newBuilder()
            .maximumSize(MAX_CACHE_SIZE)
            .build();

    private final Cache<DepositWithdrawKey, TransactionSums> depositWithdrawCache =
            Caffeine.newBuilder()
                    .maximumSize(MAX_CACHE_SIZE)
                    .build();

    public RecommendationsRepository(
            @Qualifier("recommendationsJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int countTransactions(UUID userId, ProductType productType) {
        CountKey key = new CountKey(userId, productType);

        return countCache.get(key, ignored -> {
            Integer count = jdbcTemplate.queryForObject(
                    COUNT_TRANSACTIONS_SQL,
                    Integer.class,
                    userId,
                    productType.name()
            );
            return count != null ? count : 0;
        });
    }

    public int sumTransactions(
            UUID userId,
            ProductType productType,
            TransactionType transactionType) {

        SumKey key = new SumKey(userId, productType, transactionType);

        return sumCache.get(key, ignored -> {
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
        DepositWithdrawKey key = new DepositWithdrawKey(userId, productType);

        return depositWithdrawCache.get(key, ignored ->
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

    private record CountKey(UUID userId, ProductType productType) {
    }

    private record SumKey(
            UUID userId,
            ProductType productType,
            TransactionType transactionType) {
    }

    private record DepositWithdrawKey(UUID userId, ProductType productType) {
    }
}