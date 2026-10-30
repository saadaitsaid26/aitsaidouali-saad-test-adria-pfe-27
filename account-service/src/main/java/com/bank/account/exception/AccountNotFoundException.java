// Exception métier : compte introuvable (404)
package com.bank.account.exception;

import java.util.UUID;

/** Levée quand un compte est recherché par un id inexistant. */
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(UUID accountId) {
        super("Compte introuvable : " + accountId);
    }
}
