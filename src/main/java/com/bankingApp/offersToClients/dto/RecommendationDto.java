package com.bankingApp.offersToClients.dto;

import java.util.UUID;

/**
 * Single recommended product.
 *
 * @param name short product name, for example {@code Invest500}
 * @param id   product identifier used for de-duplication
 * @param text marketing description shown to the client
 */
public record RecommendationDto(String name, UUID id, String text) {
}
