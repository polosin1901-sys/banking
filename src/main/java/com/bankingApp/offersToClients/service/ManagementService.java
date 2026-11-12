package com.bankingApp.offersToClients.service;

import com.bankingApp.offersToClients.cache.RecommendationsCache;
import org.springframework.stereotype.Service;

@Service
public class ManagementService {

    private final RecommendationsCache recommendationsCache;

    public ManagementService(RecommendationsCache recommendationsCache) {
        this.recommendationsCache = recommendationsCache;
    }

    public void clearCaches() {
        recommendationsCache.clear();
    }
}