package com.bankingApp.offersToClients.service;

import com.bankingApp.offersToClients.cache.RecommendationsCache;
import org.springframework.stereotype.Service;

/**
 * Application management operations exposed through {@code /management}.
 */
@Service
public class ManagementService {

    private final RecommendationsCache recommendationsCache;

    public ManagementService(RecommendationsCache recommendationsCache) {
        this.recommendationsCache = recommendationsCache;
    }

    /**
     * Drops all cached recommendation aggregates.
     *
     * <p>After this call the next recommendation request reads fresh data from the recommendations
     * database instead of Caffeine.</p>
     */
    public void clearCaches() {
        recommendationsCache.clear();
    }
}
