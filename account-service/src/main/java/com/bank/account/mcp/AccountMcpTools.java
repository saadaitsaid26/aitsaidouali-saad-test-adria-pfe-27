// Outils MCP des comptes : exposition Spring AI, délégation pure à AccountService (P5)
package com.bank.account.mcp;

import java.util.List;
import java.util.UUID;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import com.bank.account.domain.AccountStatus;
import com.bank.account.service.AccountService;
import com.bank.account.service.dto.AccountResponse;
import com.bank.account.service.dto.BalanceResponse;
import com.bank.account.service.dto.CreateAccountRequest;

/**
 * Outils MCP (Model Context Protocol) du account-service, consommés par le
 * assistant-service (partie E). Chaque méthode = UN appel à la MÊME couche
 * service (AccountService) que REST/GraphQL : aucune règle métier ici.
 * Noms d'outils = contrat : listAccounts, getAccount, getAccountBalance,
 * createAccount, updateAccountStatus.
 */
@Service
public class AccountMcpTools {

    private final AccountService accountService;

    public AccountMcpTools(AccountService accountService) {
        this.accountService = accountService;
    }

    /** Liste tous les comptes avec référence, statut, devise et solde. */
    @Tool(name = "listAccounts",
          description = "List all bank accounts with their reference, status (ACTIVE/BLOCKED), currency and balance.")
    public List<AccountResponse> listAccounts() {
        return accountService.listAccounts();
    }

    /** Lit un compte par son identifiant. */
    @Tool(name = "getAccount",
          description = "Read one bank account by its identifier.")
    public AccountResponse getAccount(
            @ToolParam(description = "Unique identifier (UUID) of the bank account") UUID accountId) {
        return accountService.getAccount(accountId);
    }

    /** Lit le solde courant d'un compte. */
    @Tool(name = "getAccountBalance",
          description = "Read the current balance of one bank account.")
    public BalanceResponse getAccountBalance(
            @ToolParam(description = "Unique identifier (UUID) of the bank account") UUID accountId) {
        return accountService.getBalance(accountId);
    }

    /** Ouvre un nouveau compte bancaire. */
    @Tool(name = "createAccount",
          description = "Open a new bank account.")
    public AccountResponse createAccount(
            @ToolParam(description = "Unique business reference of the bank account (e.g. ACC-004)") String customerReference,
            @ToolParam(description = "ISO 4217 currency code, exactly 3 letters (e.g. EUR, MAD)") String currency,
            @ToolParam(description = "Initial balance of the account, must be >= 0") double initialBalance) {
        return accountService.createAccount(new CreateAccountRequest(customerReference, currency, initialBalance));
    }

    /** Met à jour le statut d'un compte (ACTIVE ou BLOCKED). */
    @Tool(name = "updateAccountStatus",
          description = "Update the status of one bank account (ACTIVE or BLOCKED).")
    public AccountResponse updateAccountStatus(
            @ToolParam(description = "Unique identifier (UUID) of the bank account") UUID accountId,
            @ToolParam(description = "Status to apply: ACTIVE (account operational) or BLOCKED (operations refused)") AccountStatus status) {
        return accountService.updateStatus(accountId, status);
    }
}
