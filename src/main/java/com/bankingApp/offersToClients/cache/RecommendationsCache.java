package com.bankingApp.offersToClients.cache;

import com.bankingApp.offersToClients.model.ProductType;
import com.bankingApp.offersToClients.model.TransactionSums;
import com.bankingApp.offersToClients.model.TransactionType;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * In-memory cache of recommendation aggregates backed by Caffeine.
 *
 * <p>Three independent caches store transaction counts, transaction sums and deposit/withdraw
 * pairs. Keys are immutable records compared by value. Loaders are invoked only on cache misses,
 * so the database is queried at most once per key until the cache is cleared.</p>
 */
@Component
public class RecommendationsCache {

    private static final long MAX_CACHE_SIZE = 10_000;

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

    /**
     * @param userId      client identifier
     * @param productType product type
     * @param loader      database query executed on a cache miss
     * @return cached transaction count
     */
    public int count(UUID userId, ProductType productType, IntSupplier loader) {
        CountKey key = new CountKey(userId, productType);
        return countCache.get(key, ignored -> loader.getAsInt());
    }

    /**
     * @param userId          client identifier
     * @param productType     product type
     * @param transactionType transaction type
     * @param loader          database query executed on a cache miss
     * @return cached transaction sum
     */
    public int sum(UUID userId,
                   ProductType productType,
                   TransactionType transactionType,
                   IntSupplier loader) {
        SumKey key = new SumKey(userId, productType, transactionType);
        return sumCache.get(key, ignored -> loader.getAsInt());
    }

    /**
     * @param userId      client identifier
     * @param productType product type
     * @param loader      database query executed on a cache miss
     * @return cached deposit and withdraw sums
     */
    public TransactionSums depositAndWithdraw(UUID userId,
                                              ProductType productType,
                                              Supplier<TransactionSums> loader) {
        DepositWithdrawKey key = new DepositWithdrawKey(userId, productType);
        return depositWithdrawCache.get(key, ignored -> loader.get());
    }

    /**
     * Drops all cached entries.
     */
    public void clear() {
        countCache.invalidateAll();
        sumCache.invalidateAll();
        depositWithdrawCache.invalidateAll();
    }

    private record CountKey(UUID userId, ProductType productType) {
    }

    private record SumKey(UUID userId,
                          ProductType productType,
                          TransactionType transactionType) {
    }

    private record DepositWithdrawKey(UUID userId, ProductType productType) {
    }
}
