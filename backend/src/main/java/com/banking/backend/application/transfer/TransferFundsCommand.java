package com.banking.backend.application.transfer;

import java.math.BigDecimal;

public record TransferFundsCommand(
        Long sourceAccountId,
        Long destinationAccountId,
        String idempotencyKey,
        BigDecimal amount) {

    public TransferFundsCommand {
        if (sourceAccountId == null) {
            throw new IllegalArgumentException("Source account ID must not be null");
        }
        if (destinationAccountId == null) {
            throw new IllegalArgumentException("Destination account ID must not be null");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key must not be blank");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}
