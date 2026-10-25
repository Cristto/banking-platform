package com.banking.backend.application.account;

import org.springframework.data.domain.Pageable;

public record GetAccountTransactionsCommand(
        Long accountId,
        Pageable pageable) {

    public GetAccountTransactionsCommand {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID must not be null");
        }
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable must not be null");
        }
    }
}
