// Exception métier : compte introuvable (404)
package com.bank.account.exception;

import java.util.UUID;

/** Levée quand un compte est recherché par un id ou une référence inexistants. */
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(UUID accountId) {
        super("Compte introuvable : " + accountId);
    }

    /** Variante recherche par référence client (le détail porte la référence). */
    public AccountNotFoundException(String customerReference) {
        super("Compte introuvable : " + customerReference);
    }
}
