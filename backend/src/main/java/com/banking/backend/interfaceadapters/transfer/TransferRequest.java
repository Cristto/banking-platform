package com.banking.backend.interfaceadapters.transfer;

import java.math.BigDecimal;

public record TransferRequest(
        Long sourceAccountId,
        Long destinationAccountId,
        String idempotencyKey,
        BigDecimal amount) {
}
