// Entité JPA Account : un compte bancaire référencé par customerReference (P4)
package com.bank.account.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Compte bancaire : référence client unique, statut (ACTIVE|BLOCKED),
 * devise et solde (double uniquement à la frontière entité/API — voir P4).
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class Account {

    /** Identifiant technique du compte (généré par l'ORM, stratégie UUID). */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Référence métier du compte côté banque, unique (ex. ACC-001). */
    @Column(nullable = false, unique = true)
    private String customerReference;

    /** Statut du compte : ACTIVE ou BLOCKED. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    /** Devise ISO du compte (ex. EUR, MAD). */
    @Column(nullable = false)
    private String currency;

    /** Solde courant : double à la frontière entité, calculs sur BigDecimal. */
    @Column(nullable = false)
    private double balance;
}
