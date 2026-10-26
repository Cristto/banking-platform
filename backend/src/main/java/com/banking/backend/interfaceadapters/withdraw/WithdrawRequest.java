package com.banking.backend.interfaceadapters.withdraw;

import java.math.BigDecimal;

public record WithdrawRequest(
        String idempotencyKey,
        BigDecimal amount) {
}
