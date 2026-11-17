package com.bankingApp.offersToClients.model;

/**
 * Deposit and withdraw sums of a client for a single product type.
 *
 * @param deposit  total deposited amount
 * @param withdraw total withdrawn amount
 */
public record TransactionSums(int deposit, int withdraw) {
}
