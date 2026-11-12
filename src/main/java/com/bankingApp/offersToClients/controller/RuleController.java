package com.bankingApp.offersToClients.controller;

import com.bankingApp.offersToClients.dto.ErrorResponse;
import com.bankingApp.offersToClients.dto.RuleListResponse;
import com.bankingApp.offersToClients.dto.RuleRequest;
import com.bankingApp.offersToClients.dto.RuleResponse;
import com.bankingApp.offersToClients.service.RuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rule")
public class RuleController {

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @PostMapping
    public ResponseEntity<RuleResponse> createRule(@RequestBody RuleRequest request) {
        return ResponseEntity.ok(ruleService.createRule(request));
    }

    @GetMapping
    public ResponseEntity<RuleListResponse> getAllRules() {
        RuleListResponse response = new RuleListResponse(ruleService.getAllRules());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable("id") Long id) {
        if (!ruleService.deleteRule(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        String message = exception.getMessage() != null
                ? exception.getMessage()
                : "Invalid rule";

        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }
}