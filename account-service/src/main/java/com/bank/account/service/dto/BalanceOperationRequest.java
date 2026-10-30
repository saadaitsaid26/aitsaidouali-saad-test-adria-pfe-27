// DTO d'opération de solde : débit ou crédit sur un compte existant (P4)
package com.bank.account.service.dto;

import java.math.BigDecimal;

import com.bank.account.domain.BalanceOperation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** Requête PATCH /api/accounts/{id}/balance : opération + montant. */
public record BalanceOperationRequest(

        /** Sens de l'opération : DEBIT ou CREDIT. */
        @NotNull(message = "L'opération est obligatoire")
        BalanceOperation operation,

        /** Montant strictement positif. */
        @NotNull(message = "Le montant est obligatoire")
        @DecimalMin(value = "0.01", message = "Le montant doit être supérieur ou égal à 0.01")
        BigDecimal amount
) {
}
