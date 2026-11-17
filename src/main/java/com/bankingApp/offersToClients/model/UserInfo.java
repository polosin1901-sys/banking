package com.bankingApp.offersToClients.model;

import java.util.UUID;

/**
 * Minimal client profile required to build a recommendation reply.
 *
 * @param id        client identifier used by the recommendation engine
 * @param username  login stored in the {@code users} table
 * @param firstName client first name
 * @param lastName  client last name
 */
public record UserInfo(
        UUID id,
        String username,
        String firstName,
        String lastName
) {
}
