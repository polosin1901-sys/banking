package com.bankingApp.offersToClients.controller;

import com.bankingApp.offersToClients.dto.RuleStatListResponse;
import com.bankingApp.offersToClients.service.RuleStatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint with dynamic rule hit statistics.
 */
@RestController
@RequestMapping("/rule/stats")
public class RuleStatController {

    private final RuleStatService ruleStatService;

    public RuleStatController(RuleStatService ruleStatService) {
        this.ruleStatService = ruleStatService;
    }

    /**
     * Returns hit counters for every rule, including rules with zero hits.
     *
     * @return statistics ordered by rule identifier
     */
    @GetMapping
    public ResponseEntity<RuleStatListResponse> getStats() {
        return ResponseEntity.ok(new RuleStatListResponse(ruleStatService.getStats()));
    }
}
