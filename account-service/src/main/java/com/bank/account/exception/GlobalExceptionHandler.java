// Gestionnaire central d'erreurs HTTP (ProblemDetail, RFC 7807) (P4)
package com.bank.account.exception;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Convertit les exceptions métier de toutes les couches web en ProblemDetail. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Compte introuvable -> 404. */
    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleNotFound(AccountNotFoundException exception) {
        log.warn("Compte introuvable : {}", exception.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Ressource introuvable");
        return problem;
    }

    /** Doublon de référence client / compte bloqué -> 409. */
    @ExceptionHandler({DuplicateAccountException.class, AccountBlockedException.class})
    public ProblemDetail handleConflict(RuntimeException exception) {
        log.warn("Requête en conflit : {}", exception.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setTitle("Conflit");
        return problem;
    }

    /** Fonds insuffisants -> 422. */
    @ExceptionHandler(InsufficientFundsException.class)
    public ProblemDetail handleInsufficientFunds(InsufficientFundsException exception) {
        log.error("Opération refusée : {}", exception.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage());
        problem.setTitle("Fonds insuffisants");
        return problem;
    }

    /** Bean Validation échouée -> 400, détail = champs incriminés. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        String detail = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validation échouée : {}", detail);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Validation failed");
        return problem;
    }
}
