// Contrat métier du account-service : même couche service pour REST/GraphQL/MCP (P4)
package com.bank.account.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.bank.account.domain.AccountStatus;
import com.bank.account.domain.BalanceOperation;
import com.bank.account.service.dto.AccountResponse;
import com.bank.account.service.dto.BalanceResponse;
import com.bank.account.service.dto.CreateAccountRequest;

/** Contrat métier des comptes : unique couche appelée par REST, GraphQL et MCP. */
public interface AccountService {

    /** Crée un compte (référence client unique) et renvoie sa vue de lecture. */
    AccountResponse createAccount(CreateAccountRequest request);

    /** Liste tous les comptes. */
    List<AccountResponse> listAccounts();

    /** Renvoie un compte par son id, ou échoue avec 404. */
    AccountResponse getAccount(UUID id);

    /** Renvoie un compte par sa référence client métier, ou échoue avec 404 (référence dans le message). */
    AccountResponse getAccountByCustomerReference(String customerReference);

    /** Renvoie le solde compact d'un compte, ou échoue avec 404. */
    BalanceResponse getBalance(UUID id);

    /** Change le statut du compte (ACTIVE <-> BLOCKED). */
    AccountResponse updateStatus(UUID id, AccountStatus status);

    /** Applique DEBIT ou CREDIT sur le solde (calculs sur BigDecimal, arrondi 2 décimales). */
    AccountResponse applyBalanceOperation(UUID id, BalanceOperation operation, BigDecimal amount);
}
