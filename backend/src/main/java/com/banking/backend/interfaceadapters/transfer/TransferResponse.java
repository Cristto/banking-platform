package com.banking.backend.interfaceadapters.transfer;

import com.banking.backend.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
        Long transactionId,
        BigDecimal amount,
        BigDecimal sourceBalance,
        BigDecimal destinationBalance,
        String currency,
        String status,
        LocalDateTime createdAt) {

    public static TransferResponse from(Transaction transaction, BigDecimal sourceBalance, BigDecimal destinationBalance) {
        return new TransferResponse(
                transaction.getId(),
                transaction.getAmount(),
                sourceBalance,
                destinationBalance,
                transaction.getSourceAccount().getCurrency(),
                transaction.getStatus().name(),
                transaction.getCreatedAt()
        );
    }
}
