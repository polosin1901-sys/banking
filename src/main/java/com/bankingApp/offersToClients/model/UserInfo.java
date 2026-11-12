package com.bankingApp.offersToClients.model;

import java.util.UUID;

public record UserInfo(
        UUID id,
        String userName,
        String firstName,
        String lastName
) {
}
