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

    public int count(UUID userId, ProductType productType, IntSupplier loader) {
        CountKey key = new CountKey(userId, productType);
        return countCache.get(key, ignored -> loader.getAsInt());
    }

    public int sum(UUID userId,
                   ProductType productType,
                   TransactionType transactionType,
                   IntSupplier loader) {
        SumKey key = new SumKey(userId, productType, transactionType);
        return sumCache.get(key, ignored -> loader.getAsInt());
    }

    public TransactionSums depositAndWithdraw(UUID userId,
                                              ProductType productType,
                                              Supplier<TransactionSums> loader) {
        DepositWithdrawKey key = new DepositWithdrawKey(userId, productType);
        return depositWithdrawCache.get(key, ignored -> loader.get());
    }

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