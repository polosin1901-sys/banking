package com.bankingApp.offersToClients.controller;

import com.bankingApp.offersToClients.dto.RuleStatDto;
import com.bankingApp.offersToClients.dto.RuleStatListResponse;
import com.bankingApp.offersToClients.service.RuleStatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rule/stats")
public class RuleStatController {

    private final RuleStatService ruleStatService;

    public RuleStatController(RuleStatService ruleStatService) {
        this.ruleStatService = ruleStatService;
    }

    @GetMapping
    public ResponseEntity<RuleStatListResponse> getStats() {
        return ResponseEntity.ok(new RuleStatListResponse(ruleStatService.getStats()));
    }
}
