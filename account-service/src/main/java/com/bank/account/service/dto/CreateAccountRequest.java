// DTO de création d'un compte (P4)
package com.bank.account.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Requête de création de compte : référence client, devise ISO, solde initial. */
public record CreateAccountRequest(

        /** Référence métier du compte côté banque, unique. */
        @NotBlank(message = "La référence client est obligatoire")
        String customerReference,

        /** Devise ISO à 3 lettres (ex. EUR, MAD). */
        @NotBlank(message = "La devise est obligatoire")
        @Size(min = 3, max = 3, message = "La devise doit contenir exactement 3 caractères")
        String currency,

        /** Solde initial >= 0. */
        @PositiveOrZero(message = "Le solde initial doit être positif ou nul")
        double initialBalance
) {
}
