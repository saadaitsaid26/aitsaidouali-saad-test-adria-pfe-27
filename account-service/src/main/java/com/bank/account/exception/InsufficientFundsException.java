// Exception métier : fonds insuffisants (422)
package com.bank.account.exception;

import java.math.BigDecimal;
import java.util.UUID;

/** Levée quand un débit ramènerait le solde du compte en dessous de zéro. */
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(UUID accountId, BigDecimal requested, double balance) {
        super("Fonds insuffisants sur le compte " + accountId
                + " : solde=" + balance + ", débit demandé=" + requested);
    }
}
