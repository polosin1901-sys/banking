package com.bankingApp.offersToClients.controller;

import com.bankingApp.offersToClients.dto.ErrorResponse;
import com.bankingApp.offersToClients.dto.RuleListResponse;
import com.bankingApp.offersToClients.dto.RuleRequest;
import com.bankingApp.offersToClients.dto.RuleResponse;
import com.bankingApp.offersToClients.service.RuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints used by the manager to maintain dynamic recommendation rules.
 */
@RestController
@RequestMapping("/rule")
public class RuleController {

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    /**
     * Creates a dynamic rule.
     *
     * @param request rule description
     * @return stored rule with its identifier
     */
    @PostMapping
    public ResponseEntity<RuleResponse> createRule(@RequestBody RuleRequest request) {
        return ResponseEntity.ok(ruleService.createRule(request));
    }

    /**
     * @return all dynamic rules
     */
    @GetMapping
    public ResponseEntity<RuleListResponse> getAllRules() {
        RuleListResponse response = new RuleListResponse(ruleService.getAllRules());
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a rule and its statistics.
     *
     * @param id rule identifier
     * @return {@code 204} when deleted, {@code 404} when the rule does not exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable("id") Long id) {
        if (!ruleService.deleteRule(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Translates validation failures into a {@code 400} response with a readable message.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        String message = exception.getMessage() != null
                ? exception.getMessage()
                : "Invalid rule";

        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }
}
