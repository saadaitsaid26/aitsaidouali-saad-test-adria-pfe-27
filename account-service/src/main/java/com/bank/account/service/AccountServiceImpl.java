// Implémentation métier des comptes : règles de gestion + logs FR (P4)
package com.bank.account.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.account.domain.Account;
import com.bank.account.domain.AccountStatus;
import com.bank.account.domain.BalanceOperation;
import com.bank.account.exception.AccountBlockedException;
import com.bank.account.exception.AccountNotFoundException;
import com.bank.account.exception.DuplicateAccountException;
import com.bank.account.exception.InsufficientFundsException;
import com.bank.account.repository.AccountRepository;
import com.bank.account.service.dto.AccountMapper;
import com.bank.account.service.dto.AccountResponse;
import com.bank.account.service.dto.BalanceResponse;
import com.bank.account.service.dto.CreateAccountRequest;

/** Runtime de la couche métier comptes : ne connaît ni web ni GraphQL ni MCP. */
@Service
public class AccountServiceImpl implements AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("Création d'un compte pour la référence client {}", request.customerReference());
        accountRepository.findByCustomerReference(request.customerReference())
                .ifPresent(existing -> {
                    log.warn("Référence client déjà utilisée : {}", request.customerReference());
                    throw new DuplicateAccountException(request.customerReference());
                });
        // Solde initial normalisé : calcul BigDecimal arrondi à 2 décimales puis conversion en double
        double balanceInitiale = BigDecimal.valueOf(request.initialBalance())
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
        Account saved = accountRepository.save(AccountMapper.toEntity(request, balanceInitiale));
        log.info("Compte créé id={} référence={} solde={}", saved.getId(),
                saved.getCustomerReference(), saved.getBalance());
        return AccountMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> listAccounts() {
        log.debug("Liste des comptes demandée");
        return accountRepository.findAll().stream()
                .map(AccountMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccount(UUID id) {
        log.info("Lecture du compte {}", id);
        return AccountMapper.toResponse(findExisting(id));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByCustomerReference(String customerReference) {
        log.info("Lecture du compte par référence client {}", customerReference);
        return accountRepository.findByCustomerReference(customerReference)
                .map(AccountMapper::toResponse)
                .orElseThrow(() -> new AccountNotFoundException(customerReference));
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceResponse getBalance(UUID id) {
        Account account = findExisting(id);
        return new BalanceResponse(account.getId(), account.getStatus().name(), account.getBalance());
    }

    @Override
    @Transactional
    public AccountResponse updateStatus(UUID id, AccountStatus status) {
        log.info("Changement de statut du compte {} vers {}", id, status);
        Account account = findExisting(id);
        account.setStatus(status);
        Account saved = accountRepository.save(account);
        return AccountMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AccountResponse applyBalanceOperation(UUID id, BalanceOperation operation, BigDecimal amount) {
        log.info("Opération {} de {} sur le compte {}", operation, amount, id);
        Account account = findExisting(id);
        if (account.getStatus() == AccountStatus.BLOCKED) {
            log.warn("Opération {} refusée : le compte {} est bloqué", operation, id);
            throw new AccountBlockedException(id);
        }
        BigDecimal current = BigDecimal.valueOf(account.getBalance());
        BigDecimal result = operation == BalanceOperation.CREDIT
                ? current.add(amount)
                : current.subtract(amount);
        if (operation == BalanceOperation.DEBIT && result.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Débit refusé sur le compte {} : solde={}, montant={}, résultat={}",
                    id, account.getBalance(), amount, result);
            throw new InsufficientFundsException(id, amount, account.getBalance());
        }
        account.setBalance(result.setScale(2, RoundingMode.HALF_UP).doubleValue());
        Account saved = accountRepository.save(account);
        log.info("Nouveau solde du compte {} : {}", id, saved.getBalance());
        return AccountMapper.toResponse(saved);
    }

    /** Charge le compte ou lève l'exception 404 conventionnelle. */
    private Account findExisting(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }
}
