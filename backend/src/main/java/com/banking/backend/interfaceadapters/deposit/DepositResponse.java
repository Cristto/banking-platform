package com.banking.backend.interfaceadapters.deposit;

import com.banking.backend.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DepositResponse(
        Long transactionId,
        BigDecimal amount,
        BigDecimal balance,
        String currency,
        String status,
        LocalDateTime createdAt) {

    public static DepositResponse from(Transaction transaction, BigDecimal balance) {
        return new DepositResponse(
                transaction.getId(),
                transaction.getAmount(),
                balance,
                transaction.getDestinationAccount().getCurrency(),
                transaction.getStatus().name(),
                transaction.getCreatedAt()
        );
    }
}
