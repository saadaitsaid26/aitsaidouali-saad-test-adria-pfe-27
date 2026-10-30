// Enum des statuts possibles d'un compte bancaire (P4)
package com.bank.account.domain;

/** Statuts du cycle de vie d'un compte : actif ou bloqué. */
public enum AccountStatus {
    /** Compte opérationnel : débits et crédits autorisés. */
    ACTIVE,
    /** Compte bloqué : toute opération de solde est refusée (409). */
    BLOCKED
}
