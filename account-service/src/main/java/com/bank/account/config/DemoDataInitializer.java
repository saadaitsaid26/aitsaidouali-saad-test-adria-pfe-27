// Données de démonstration insérées au démarrage si la table est vide (P4)
package com.bank.account.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.bank.account.domain.Account;
import com.bank.account.domain.AccountStatus;
import com.bank.account.repository.AccountRepository;

/** Alimente la base H2 avec 3 comptes de démonstration (toujours actif, idempotent). */
@Component
public class DemoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final AccountRepository accountRepository;

    public DemoDataInitializer(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void run(String... args) {
        if (accountRepository.count() > 0) {
            log.debug("Données de démonstration déjà présentes : initialisation ignorée");
            return;
        }
        accountRepository.save(account("ACC-001", AccountStatus.ACTIVE, "EUR", 5000.00));
        accountRepository.save(account("ACC-002", AccountStatus.ACTIVE, "EUR", 750.00));
        accountRepository.save(account("ACC-003", AccountStatus.BLOCKED, "MAD", 10000.00));
        log.info("3 comptes de démonstration créés (ACC-001, ACC-002, ACC-003)");
    }

    /** Fabrique un compte à partir des attributs de démonstration. */
    private Account account(String customerReference, AccountStatus status, String currency, double balance) {
        Account account = new Account();
        account.setCustomerReference(customerReference);
        account.setStatus(status);
        account.setCurrency(currency);
        account.setBalance(balance);
        return account;
    }
}
