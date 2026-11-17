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

/**
 * Service endpoints used by an external system to inspect and maintain the application.
 */
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
        // BuildProperties exists only when the build-info goal ran (Maven build), not in plain IDE runs.
        this.buildProperties = buildPropertiesProvider.getIfAvailable();
        this.applicationName = applicationName;
        this.managementService = managementService;
    }

    /**
     * Returns the application name and version taken from {@code pom.xml}.
     *
     * @return name and version; the version falls back to {@code unknown} when build information
     *         is not available
     */
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

    /**
     * Drops all recommendation caches.
     *
     * @return {@code 204 No Content} with an empty body
     */
    @PostMapping("/clear-caches")
    public ResponseEntity<Void> clearCaches() {
        managementService.clearCaches();
        return ResponseEntity.noContent().build();
    }
}
