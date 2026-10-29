package com.banking.backend.application.purchase;

import org.springframework.data.domain.Pageable;

public record ListPurchasesCommand(
        Long accountId,
        Pageable pageable) {

    public ListPurchasesCommand {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID must not be null");
        }
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable must not be null");
        }
    }
}
