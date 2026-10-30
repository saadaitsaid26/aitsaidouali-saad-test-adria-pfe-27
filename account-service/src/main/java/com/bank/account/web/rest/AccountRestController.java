// Contrôleur REST des comptes : délègue 100% à AccountService (P4)
package com.bank.account.web.rest;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.bank.account.service.AccountService;
import com.bank.account.service.dto.AccountResponse;
import com.bank.account.service.dto.BalanceOperationRequest;
import com.bank.account.service.dto.BalanceResponse;
import com.bank.account.service.dto.CreateAccountRequest;
import com.bank.account.service.dto.StatusChangeRequest;

import jakarta.validation.Valid;

/**
 * API REST /api/accounts. Convertit les erreurs métier en codes HTTP via
 * GlobalExceptionHandler (DuplicateAccountException -> 409, etc.).
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountRestController {

    private final AccountService accountService;

    public AccountRestController(AccountService accountService) {
        this.accountService = accountService;
    }

    /** Crée un compte : 201 + Location /api/accounts/{id}. */
    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    /** Liste tous les comptes. */
    @GetMapping
    public List<AccountResponse> list() {
        return accountService.listAccounts();
    }

    /** Renvoie un compte par id (404 si introuvable). */
    @GetMapping("/{id}")
    public AccountResponse get(@PathVariable UUID id) {
        return accountService.getAccount(id);
    }

    /** Renvoie le solde compact d'un compte. */
    @GetMapping("/{id}/balance")
    public BalanceResponse getBalance(@PathVariable UUID id) {
        return accountService.getBalance(id);
    }

    /** Change le statut du compte (ACTIVE <-> BLOCKED). */
    @PatchMapping("/{id}/status")
    public AccountResponse updateStatus(@PathVariable UUID id,
                                        @Valid @RequestBody StatusChangeRequest request) {
        return accountService.updateStatus(id, request.status());
    }

    /** Applique un débit ou un crédit sur le solde (409 si bloqué, 422 si fonds insuffisants). */
    @PatchMapping("/{id}/balance")
    public AccountResponse applyBalanceOperation(@PathVariable UUID id,
                                                 @Valid @RequestBody BalanceOperationRequest request) {
        return accountService.applyBalanceOperation(id, request.operation(), request.amount());
    }
}
