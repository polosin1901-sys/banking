package com.bankingApp.offersToClients.dto;

/**
 * Error payload returned for invalid requests.
 *
 * @param error human readable error message
 */
public record ErrorResponse(String error) {
}
