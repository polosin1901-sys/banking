package com.bankingApp.offersToClients.api;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the static OpenAPI document.
 *
 * <p>The specification is maintained by hand, so the test makes sure it stays valid YAML, keeps
 * the published version and describes every REST endpoint of the application.</p>
 */
@SuppressWarnings("unchecked")
class OpenApiSpecificationTest {

    private Map<String, Object> specification() {
        try (InputStream stream = getClass().getResourceAsStream("/static/openapi.yaml")) {
            assertThat(stream)
                    .as("docs/api/openapi.yaml must be copied to the classpath by the build")
                    .isNotNull();
            return new Yaml().load(stream);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot read the OpenAPI specification", exception);
        }
    }

    @Test
    void declaresVersionAndServer() {
        Map<String, Object> specification = specification();

        assertThat(specification.get("openapi")).isEqualTo("3.0.3");
        assertThat(specification.get("info")).isInstanceOf(Map.class);
        assertThat(specification.get("servers")).isNotNull();
    }

    @Test
    void describesEveryEndpoint() {
        Map<String, Object> paths = (Map<String, Object>) specification().get("paths");

        assertThat(paths).containsOnlyKeys(
                "/recommendation/{user_id}",
                "/rule",
                "/rule/{id}",
                "/rule/stats",
                "/management/info",
                "/management/clear-caches"
        );
        assertThat((Map<String, Object>) paths.get("/rule"))
                .containsKeys("get", "post");
        assertThat((Map<String, Object>) paths.get("/rule/{id}"))
                .containsKey("delete");
    }

    @Test
    void declaresReusableSchemas() {
        Map<String, Object> components = (Map<String, Object>) specification().get("components");
        Map<String, Object> schemas = (Map<String, Object>) components.get("schemas");

        assertThat(schemas).containsKeys(
                "Recommendation",
                "RecommendationResponse",
                "Query",
                "RuleRequest",
                "RuleResponse",
                "RuleStat",
                "RuleStatListResponse",
                "ManagementInfo",
                "ErrorResponse"
        );
    }
}
