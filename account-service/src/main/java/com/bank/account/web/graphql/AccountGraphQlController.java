// Contrôleur GraphQL des comptes : délègue 100% à AccountService (P5) — zéro logique métier
package com.bank.account.web.graphql;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.bank.account.domain.AccountStatus;
import com.bank.account.exception.AccountBlockedException;
import com.bank.account.exception.AccountNotFoundException;
import com.bank.account.exception.DuplicateAccountException;
import com.bank.account.exception.InsufficientFundsException;
import com.bank.account.service.AccountService;
import com.bank.account.service.dto.AccountResponse;
import com.bank.account.service.dto.CreateAccountRequest;

import graphql.ErrorType;
import graphql.GraphQLError;

/**
 * Requêtes/mutations GraphQL /graphql. Appelle EXACTEMENT la même couche
 * service (AccountService) que le contrôleur REST : aucune règle métier ici.
 * Les exceptions métier sont converties en erreurs GraphQL lisibles (avec
 * extensions code) via les handlers ci-dessous, spécifiques à ce contrôleur.
 */
@Controller
public class AccountGraphQlController {

    private static final Logger log = LoggerFactory.getLogger(AccountGraphQlController.class);

    private final AccountService accountService;

    public AccountGraphQlController(AccountService accountService) {
        this.accountService = accountService;
    }

    /** query { accounts { ... } } : liste de tous les comptes. */
    @QueryMapping
    public List<AccountResponse> accounts() {
        return accountService.listAccounts();
    }

    /** query { accountById(id: "...") } : un compte par id (404-like si absent). */
    @QueryMapping
    public AccountResponse accountById(@Argument UUID id) {
        return accountService.getAccount(id);
    }

    /** query { accountByCustomerReference(customerReference: "...") } : un compte par référence métier. */
    @QueryMapping
    public AccountResponse accountByCustomerReference(@Argument String customerReference) {
        return accountService.getAccountByCustomerReference(customerReference);
    }

    /** mutation { createAccount(account: { ... }) } : ouverture d'un compte. */
    @MutationMapping
    public AccountResponse createAccount(@Argument CreateAccountInput account) {
        log.info("Mutation createAccount pour la référence {}", account.customerReference());
        return accountService.createAccount(new CreateAccountRequest(
                account.customerReference(), account.currency(), account.initialBalanceOrZero()));
    }

    /** mutation { updateAccountStatus(id: "...", status: BLOCKED) } : ACTIVE <-> BLOCKED. */
    @MutationMapping
    public AccountResponse updateAccountStatus(@Argument UUID id, @Argument AccountStatus status) {
        return accountService.updateStatus(id, status);
    }

    /** Compte introuvable -> erreur lisible (classification "donnée absente", code machine en extensions). */
    @GraphQlExceptionHandler
    public GraphQLError handleNotFound(AccountNotFoundException exception) {
        log.warn("GraphQL : {}", exception.getMessage());
        return GraphQLError.newError()
                .errorType(ErrorType.DataFetchingException)
                .message(exception.getMessage())
                .extensions(Map.of("code", "ACCOUNT_NOT_FOUND"))
                .build();
    }

    /** Doublon / compte bloqué / fonds insuffisants -> erreur lisible (validation, code machine en extensions). */
    @GraphQlExceptionHandler
    public GraphQLError handleConflict(RuntimeException exception) {
        log.warn("GraphQL : {}", exception.getMessage());
        String code = exception instanceof DuplicateAccountException ? "DUPLICATE_ACCOUNT"
                : exception instanceof AccountBlockedException ? "ACCOUNT_BLOCKED"
                : exception instanceof InsufficientFundsException ? "INSUFFICIENT_FUNDS"
                : "BUSINESS_ERROR";
        return GraphQLError.newError()
                .errorType(ErrorType.ValidationError)
                .message(exception.getMessage())
                .extensions(Map.of("code", code))
                .build();
    }

    /** Requête invalide (validation bean ou entrée incohérente) -> erreur lisible générique. */
    @GraphQlExceptionHandler
    public GraphQLError handleInvalidRequest(Exception exception) {
        log.warn("GraphQL : requête invalide ({})", exception.getMessage());
        return GraphQLError.newError()
                .errorType(ErrorType.ValidationError)
                .message(exception.getMessage() == null || exception.getMessage().isBlank()
                        ? "Requête invalide" : exception.getMessage())
                .extensions(Map.of("code", "VALIDATION_ERROR"))
                .build();
    }
}
