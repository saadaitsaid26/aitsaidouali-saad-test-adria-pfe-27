// Tests d'intégration MockMvc du contrôleur REST des comptes (P4)
package com.bank.account.web.rest;

import java.math.BigDecimal;
import java.util.List;

import com.bank.account.service.dto.AccountResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie la API REST de bout en bout : 201+Location, 200, 404, 422 (fonds
 * insuffisants), 409 (compte bloqué). Eureka désactivé pour le test.
 */
@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
class AccountRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /** Récupère la liste courante des comptes (graine de DemoDataInitializer). */
    private List<AccountResponse> listAccounts() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<AccountResponse>>() { });
    }

    @Test
    void createAccountReturns201WithLocation() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerReference":"ACC-TEST-42","currency":"EUR","initialBalance":100.0}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/accounts/")))
                .andExpect(jsonPath("$.customerReference").value("ACC-TEST-42"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(100.0));
    }

    @Test
    void listAccountsReturnsNonEmptyListSeededByDemoData() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(org.hamcrest.Matchers.greaterThanOrEqualTo(3))));
    }

    @Test
    void getUnknownAccountReturns404() throws Exception {
        mockMvc.perform(get("/api/accounts/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void excessiveDebitReturns422() throws Exception {
        List<AccountResponse> accounts = listAccounts();
        assertThat(accounts).isNotEmpty();
        String id = accounts.get(0).id().toString();
        double balance = accounts.get(0).balance();

        mockMvc.perform(patch("/api/accounts/{id}/balance", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"operation\":\"DEBIT\"," +
                                "\"amount\":" + BigDecimal.valueOf(balance + 1000.00) +
                                "}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void debitOnBlockedAccountReturns409() throws Exception {
        List<AccountResponse> accounts = listAccounts();
        assertThat(accounts).isNotEmpty();
        String id = accounts.get(1).id().toString();
        mockMvc.perform(patch("/api/accounts/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"BLOCKED\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/accounts/{id}/balance", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"operation\":\"DEBIT\",\"amount\":10.00}"))
                .andExpect(status().isConflict());
    }
}
