package com.bankingApp.offersToClients.service.dynamic;

import com.bankingApp.offersToClients.model.QueryType;

import java.util.List;
import java.util.UUID;

public interface QueryEvaluator {

    QueryType type();

    boolean evaluate(UUID userId, List<String> arguments);
}