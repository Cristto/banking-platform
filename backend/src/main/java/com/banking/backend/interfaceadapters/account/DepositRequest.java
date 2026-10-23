package com.banking.backend.interfaceadapters.account;

import java.math.BigDecimal;

public record DepositRequest(
        String idempotencyKey,
        BigDecimal amount) {
}
