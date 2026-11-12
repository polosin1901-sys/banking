package com.bankingApp.offersToClients.controller;

import com.bankingApp.offersToClients.dto.ManagementInfoResponse;
import com.bankingApp.offersToClients.service.ManagementService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/management")
public class ManagementController {

    private static final String UNKNOWN_VERSION = "unknown";

    private final BuildProperties buildProperties;
    private final String applicationName;
    private final ManagementService managementService;

    public ManagementController(
            ObjectProvider<BuildProperties> buildPropertiesProvider,
            @Value("${spring.application.name:offersToClients}") String applicationName,
            ManagementService managementService) {
        this.buildProperties = buildPropertiesProvider.getIfAvailable();
        this.applicationName = applicationName;
        this.managementService = managementService;
    }

    @GetMapping("/info")
    public ManagementInfoResponse getInfo() {
        if (buildProperties == null) {
            return new ManagementInfoResponse(applicationName, UNKNOWN_VERSION);
        }

        return new ManagementInfoResponse(
                buildProperties.getArtifact(),
                buildProperties.getVersion()
        );
    }

    @PostMapping("/clear-caches")
    public ResponseEntity<Void> clearCaches() {
        managementService.clearCaches();
        return ResponseEntity.noContent().build();
    }
}