package com.banking.backend.application.account;

import java.math.BigDecimal;

public record DepositFundsCommand(
        Long accountId,
        String idempotencyKey,
        BigDecimal amount) {

    public DepositFundsCommand {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID must not be null");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key must not be blank");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}
