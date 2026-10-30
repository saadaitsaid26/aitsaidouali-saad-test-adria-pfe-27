// DTO d'entrée GraphQL pour la création d'un compte (P5) — miroir du schéma CreateAccountInput
package com.bank.account.web.graphql;

/**
 * Input GraphQL de création d'un compte : converti en CreateAccountRequest
 * (couche service) sans y toucher au reste du contrat REST.
 */
public record CreateAccountInput(
        String customerReference,
        String currency,
        Double initialBalance
) {
    /** Solde initial explicite ou 0.0 si non fourni (schema : Float nullable). */
    double initialBalanceOrZero() {
        return initialBalance == null ? 0.0 : initialBalance;
    }
}
