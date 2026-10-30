// Exception métier : compte bloqué, opération refusée (409)
package com.bank.account.exception;

import java.util.UUID;

/** Levée quand une opération de solde est demandée sur un compte BLOCKED. */
public class AccountBlockedException extends RuntimeException {

    public AccountBlockedException(UUID accountId) {
        super("Le compte " + accountId + " est bloqué : opération refusée");
    }
}
