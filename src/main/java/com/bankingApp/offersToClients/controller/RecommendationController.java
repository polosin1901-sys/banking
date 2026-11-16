package com.bankingApp.offersToClients.controller;

import com.bankingApp.offersToClients.dto.RecommendationDto;
import com.bankingApp.offersToClients.dto.RecommendationResponse;
import com.bankingApp.offersToClients.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST endpoint that returns product recommendations for a client.
 */
@RestController
@RequestMapping("/recommendation")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Returns static and dynamic recommendations for the given client.
     *
     * @param userId client identifier
     * @return client identifier together with the list of recommended products
     */
    @GetMapping("/{user_id}")
    public ResponseEntity<RecommendationResponse> getRecommendations(
            @PathVariable("user_id") UUID userId) {
        List<RecommendationDto> recommendations = recommendationService.getRecommendations(userId);
        return ResponseEntity.ok(new RecommendationResponse(userId, recommendations));
    }
}
