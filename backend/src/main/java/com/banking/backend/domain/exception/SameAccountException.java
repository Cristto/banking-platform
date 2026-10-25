package com.banking.backend.domain.exception;

public class SameAccountException extends RuntimeException {

    public SameAccountException(Long accountId) {
        super("Cannot transfer to the same account: " + accountId);
    }
}
