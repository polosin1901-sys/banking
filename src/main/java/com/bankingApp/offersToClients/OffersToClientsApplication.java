package com.bankingApp.offersToClients;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the recommendation service.
 *
 * <p>The application exposes a REST API for recommendations and rule management, a statistics
 * endpoint, management endpoints and an optional Telegram bot.</p>
 */
@SpringBootApplication
public class OffersToClientsApplication {

    public static void main(String[] args) {
        SpringApplication.run(OffersToClientsApplication.class, args);
    }
}
