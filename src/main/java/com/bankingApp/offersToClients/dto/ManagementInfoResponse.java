package com.bankingApp.offersToClients.dto;

/**
 * Build information returned by {@code GET /management/info}.
 *
 * @param name    Maven artifact identifier of the application
 * @param version Maven project version
 */
public record ManagementInfoResponse(
        String name,
        String version
) {
}
