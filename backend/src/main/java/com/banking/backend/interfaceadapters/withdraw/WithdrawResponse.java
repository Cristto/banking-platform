package com.banking.backend.interfaceadapters.withdraw;

import com.banking.backend.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WithdrawResponse(
        Long transactionId,
        BigDecimal amount,
        BigDecimal balance,
        String currency,
        String status,
        LocalDateTime createdAt) {

    public static WithdrawResponse from(Transaction transaction, BigDecimal balance) {
        return new WithdrawResponse(
                transaction.getId(),
                transaction.getAmount(),
                balance,
                transaction.getSourceAccount().getCurrency(),
                transaction.getStatus().name(),
                transaction.getCreatedAt()
        );
    }
}
