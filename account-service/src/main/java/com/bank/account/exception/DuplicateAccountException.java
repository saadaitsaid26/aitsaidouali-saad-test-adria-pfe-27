// Exception métier : référence client déjà utilisée (409)
package com.bank.account.exception;

/** Levée quand on tente de créer un compte avec une référence client déjà présente. */
public class DuplicateAccountException extends RuntimeException {

    public DuplicateAccountException(String customerReference) {
        super("Un compte existe déjà pour la référence client : " + customerReference);
    }
}
