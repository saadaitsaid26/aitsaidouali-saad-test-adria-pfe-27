// Tests slice GraphQL du contrôleur (P5) : @GraphQlTest + @MockitoBean + GraphQlTester
package com.bank.account.web.rest;

import java.util.UUID;

import com.bank.account.service.AccountService;
import com.bank.account.service.dto.AccountResponse;
import com.bank.account.service.dto.CreateAccountRequest;
import com.bank.account.web.graphql.AccountGraphQlController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import graphql.GraphQLError;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Vérifie que le contrôleur GraphQL délègue à AccountService (mocké) et que
 * les erreurs métier (ex. compte introuvable) donnent une erreur GraphQL
 * lisible. Slice limité : seul AccountGraphQlController est chargé.
 */
@GraphQlTest(AccountGraphQlController.class)
class AccountGraphQlControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    // Boot 3.5 : @MockitoBean (org.springframework.test.context.bean.override.mockito)
    @MockitoBean
    private AccountService accountService;

    /** Fabrique une réponse de compte cohérente avec la couche service. */
    private AccountResponse accountResponse(UUID id, String reference, String status, double balance) {
        return new AccountResponse(id, reference, status, "EUR", balance);
    }

    @Test
    void accountsQueryReturnsMockedAccounts() {
        UUID id = UUID.randomUUID();
        when(accountService.listAccounts()).thenReturn(java.util.List.of(
                accountResponse(id, "ACC-001", "ACTIVE", 5000.00),
                accountResponse(UUID.randomUUID(), "ACC-002", "ACTIVE", 750.00)));

        graphQlTester.document("query { accounts { id customerReference status currency balance } }")
                .execute()
                .path("accounts[0].customerReference").entity(String.class).isEqualTo("ACC-001")
                .path("accounts[1].customerReference").entity(String.class).isEqualTo("ACC-002")
                .path("accounts[0].status").entity(String.class).isEqualTo("ACTIVE")
                .path("accounts[0].balance").entity(Double.class).isEqualTo(5000.00);
    }

    @Test
    void accountByIdReturnsAccountData() {
        UUID id = UUID.randomUUID();
        when(accountService.getAccount(id)).thenReturn(accountResponse(id, "ACC-001", "ACTIVE", 5000.00));

        graphQlTester.document("query { accountById(id: \"" + id + "\") { id customerReference } }")
                .execute()
                .path("accountById.id").entity(String.class).isEqualTo(id.toString())
                .path("accountById.customerReference").entity(String.class).isEqualTo("ACC-001");
    }

    @Test
    void accountByIdUnknownProducesReadableError() {
        UUID unknownId = UUID.randomUUID();
        when(accountService.getAccount(unknownId)).thenThrow(
                new com.bank.account.exception.AccountNotFoundException(unknownId));

        graphQlTester.document("query { accountById(id: \"" + unknownId + "\") { id } }")
                .execute()
                .errors()
                .satisfy(errors -> {
                    assertThat(errors).isNotEmpty();
                    String message = errors.get(0).getMessage();
                    assertThat(message).isNotEmpty();
                });
    }

    @Test
    void createAccountMutationReturnsCreatedAccount() {
        when(accountService.createAccount(any(CreateAccountRequest.class)))
                .thenAnswer(invocation -> {
                    CreateAccountRequest request = invocation.getArgument(0);
                    return accountResponse(UUID.randomUUID(), request.customerReference(),
                            "ACTIVE", request.initialBalance());
                });

        graphQlTester.document(
                        "mutation { createAccount(account: { customerReference: \"ACC-GraphQL\", "
                                + "currency: \"EUR\", initialBalance: 500 }) { id customerReference balance } }")
                .execute()
                .path("createAccount.customerReference").entity(String.class).isEqualTo("ACC-GraphQL")
                .path("createAccount.balance").entity(Double.class).isEqualTo(500.00);
    }
}
