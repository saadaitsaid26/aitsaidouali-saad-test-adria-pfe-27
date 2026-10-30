// Tests unitaires pure Mockito de la couche service comptes (P4) — aucun contexte Spring
package com.bank.account.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bank.account.domain.Account;
import com.bank.account.domain.AccountStatus;
import com.bank.account.domain.BalanceOperation;
import com.bank.account.exception.AccountBlockedException;
import com.bank.account.exception.AccountNotFoundException;
import com.bank.account.exception.DuplicateAccountException;
import com.bank.account.exception.InsufficientFundsException;
import com.bank.account.repository.AccountRepository;
import com.bank.account.service.dto.AccountResponse;
import com.bank.account.service.dto.CreateAccountRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Vérifie les règles métier (unicité, statut, solde arrondi 2 décimales) via Mockito. */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountServiceImpl(accountRepository);
    }

    /** Fabrique une entité de test. */
    private Account account(UUID id, AccountStatus status, double balance) {
        Account entity = new Account();
        entity.setId(id);
        entity.setCustomerReference("ACC-001");
        entity.setStatus(status);
        entity.setCurrency("EUR");
        entity.setBalance(balance);
        return entity;
    }

    @Test
    void createAccountOkMapsFieldsAndGeneratesId() {
        when(accountRepository.findByCustomerReference("ACC-001")).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account toSave = invocation.getArgument(0);
            if (toSave.getId() == null) {
                toSave.setId(UUID.randomUUID()); // simulation de la génération en base
            }
            return toSave;
        });

        AccountResponse response = accountService.createAccount(
                new CreateAccountRequest("ACC-001", "EUR", 5000.00));

        assertThat(response.id()).isNotNull();
        assertThat(response.customerReference()).isEqualTo("ACC-001");
        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.currency()).isEqualTo("EUR");
        assertThat(response.balance()).isCloseTo(5000.00, within(0.001));
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccountDuplicateThrowsAndDoesNotSave() {
        when(accountRepository.findByCustomerReference("ACC-001"))
                .thenReturn(Optional.of(account(UUID.randomUUID(), AccountStatus.ACTIVE, 100.00)));

        assertThatThrownBy(() -> accountService.createAccount(
                new CreateAccountRequest("ACC-001", "EUR", 100.00)))
                .isInstanceOf(DuplicateAccountException.class)
                .hasMessageContaining("ACC-001");
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void getAccountUnknownThrowsNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(accountRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount(unknownId))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining(unknownId.toString());
    }

    @Test
    void getAccountByCustomerReferenceReturnsMappedResponse() {
        Account entity = account(UUID.randomUUID(), AccountStatus.ACTIVE, 123.45);
        when(accountRepository.findByCustomerReference("ACC-001")).thenReturn(Optional.of(entity));

        AccountResponse response = accountService.getAccountByCustomerReference("ACC-001");

        assertThat(response.customerReference()).isEqualTo("ACC-001");
        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.balance()).isCloseTo(123.45, within(0.0001));
    }

    @Test
    void getAccountByCustomerReferenceUnknownThrowsWithReferenceInMessage() {
        when(accountRepository.findByCustomerReference("ACC-404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountByCustomerReference("ACC-404"))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("ACC-404");
    }

    @Test
    void applyBalanceOperationCreditAddsAndRoundsToTwoDecimals() {
        UUID accountId = UUID.randomUUID();
        Account entity = account(accountId, AccountStatus.ACTIVE, 10.005);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(entity));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse response = accountService.applyBalanceOperation(
                accountId, BalanceOperation.CREDIT, new BigDecimal("0.004"));

        // 10.005 + 0.004 = 10.009 -> arrondi 2 décimales = 10.01
        assertThat(response.balance()).isCloseTo(10.01, within(0.0001));
    }

    @Test
    void applyBalanceOperationDebitReducesBalance() {
        UUID accountId = UUID.randomUUID();
        Account entity = account(accountId, AccountStatus.ACTIVE, 100.00);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(entity));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse response = accountService.applyBalanceOperation(
                accountId, BalanceOperation.DEBIT, new BigDecimal("40.50"));

        assertThat(response.balance()).isCloseTo(59.50, within(0.0001));
    }

    @Test
    void applyBalanceOperationDebitInsufficientFundsThrowsAndDoesNotSave() {
        UUID accountId = UUID.randomUUID();
        Account entity = account(accountId, AccountStatus.ACTIVE, 100.00);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> accountService.applyBalanceOperation(
                accountId, BalanceOperation.DEBIT, new BigDecimal("150.00")))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("Fonds insuffisants");
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void applyBalanceOperationOnBlockedAccountThrows() {
        UUID accountId = UUID.randomUUID();
        Account entity = account(accountId, AccountStatus.BLOCKED, 100.00);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> accountService.applyBalanceOperation(
                accountId, BalanceOperation.CREDIT, new BigDecimal("10.00")))
                .isInstanceOf(AccountBlockedException.class)
                .hasMessageContaining(accountId.toString());
        verify(accountRepository, never()).save(any(Account.class));
    }
}
