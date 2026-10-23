package com.banking.backend.application.account;

public record GetAccountCommand(
        Long accountId) {

    public GetAccountCommand {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID must not be null");
        }
    }
}
