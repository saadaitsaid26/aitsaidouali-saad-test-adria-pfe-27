// Mapper statique Entity <-> DTO des comptes (P4)
package com.bank.account.service.dto;

import com.bank.account.domain.Account;
import com.bank.account.domain.AccountStatus;

/** Convertisseur utilitaire Account <-> DTO, partagé par REST/GraphQL/MCP. */
public final class AccountMapper {

    private AccountMapper() {
        // Classe utilitaire : interdiction d'instanciation
    }

    /** Convertit une entité Account en DTO de lecture. */
    public static AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getCustomerReference(),
                account.getStatus().name(),
                account.getCurrency(),
                account.getBalance());
    }

    /** Convertit une requête de création en entité Account (statut initial ACTIVE, solde déjà normalisé). */
    public static Account toEntity(CreateAccountRequest request, double balance) {
        Account account = new Account();
        account.setCustomerReference(request.customerReference());
        account.setCurrency(request.currency());
        account.setBalance(balance);
        account.setStatus(AccountStatus.ACTIVE);
        return account;
    }
}
