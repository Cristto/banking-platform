package com.banking.backend.interfaceadapters.deposit;

import java.math.BigDecimal;

public record DepositRequest(
        String idempotencyKey,
        BigDecimal amount) {
}
