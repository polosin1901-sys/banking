package com.bankingApp.offersToClients.dto;

import java.util.List;
import java.util.UUID;

public record RecommendationResponse(
        UUID user_Id,
        List<RecommendationDto> recommendations) {}
