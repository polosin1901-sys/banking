package com.bankingApp.offersToClients.controller;

import com.bankingApp.offersToClients.dto.QueryDto;
import com.bankingApp.offersToClients.dto.RuleRequest;
import com.bankingApp.offersToClients.dto.RuleResponse;
import com.bankingApp.offersToClients.dto.RuleStatListResponse;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end check of the rule lifecycle: create, read statistics, delete.
 *
 * <p>The test uses the configured PostgreSQL database and removes the rule it creates. It guards
 * the rule persistence mapping, in particular the {@code position} column of {@code rule_query}.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class RuleApiIntegrationTest {

    private static final UUID PRODUCT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createsCountsAndDeletesRule() throws Exception {
        RuleRequest request = new RuleRequest(
                "Integration test rule",
                PRODUCT_ID,
                "Created by RuleApiIntegrationTest",
                List.of(new QueryDto("USER_OF", List.of("INVEST"), false))
        );

        MvcResult created = mockMvc.perform(post("/rule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        RuleResponse rule = objectMapper.readValue(
                created.getResponse().getContentAsString(), RuleResponse.class);

        assertThat(rule.id()).isNotNull();
        assertThat(rule.rule()).hasSize(1);
        assertThat(rule.rule().get(0).arguments()).containsExactly("INVEST");

        Long ruleId = rule.id();
        try {
            MvcResult statsResult = mockMvc.perform(get("/rule/stats"))
                    .andExpect(status().isOk())
                    .andReturn();

            RuleStatListResponse stats = objectMapper.readValue(
                    statsResult.getResponse().getContentAsString(), RuleStatListResponse.class);

            assertThat(stats.stats())
                    .anySatisfy(stat -> {
                        assertThat(stat.ruleId()).isEqualTo(ruleId);
                        assertThat(stat.count()).isZero();
                    });
        } finally {
            mockMvc.perform(delete("/rule/{id}", ruleId))
                    .andExpect(status().isNoContent());
        }

        mockMvc.perform(delete("/rule/{id}", ruleId))
                .andExpect(status().isNotFound());
    }
}
