// DTO de changement de statut d'un compte (P4)
package com.bank.account.service.dto;

import com.bank.account.domain.AccountStatus;

import jakarta.validation.constraints.NotNull;

/** Requête PATCH /api/accounts/{id}/status : nouveau statut du compte. */
public record StatusChangeRequest(

        /** Nouveau statut : ACTIVE ou BLOCKED. */
        @NotNull(message = "Le statut est obligatoire")
        AccountStatus status
) {
}
