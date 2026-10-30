// Repository JPA des comptes (P4)
package com.bank.account.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bank.account.domain.Account;

/** Accès aux données des comptes : CRUD standard + recherche par référence client. */
@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    /** Recherche un compte par sa référence client métier (unique). */
    Optional<Account> findByCustomerReference(String customerReference);
}
