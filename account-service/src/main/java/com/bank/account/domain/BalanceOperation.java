// Enum des opérations de solde applicables à un compte (P4)
package com.bank.account.domain;

/** Sens d'une opération de solde : débit (soustrait) ou crédit (ajoute). */
public enum BalanceOperation {
    /** Débit : diminue le solde du montant demandé. */
    DEBIT,
    /** Crédit : augmente le solde du montant demandé. */
    CREDIT
}
