package com.banking.backend.domain.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Long accountId) {
        super("Account with id " + accountId + " not found");
    }
}
