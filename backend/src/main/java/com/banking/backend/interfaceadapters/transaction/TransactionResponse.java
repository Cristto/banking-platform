package com.banking.backend.interfaceadapters.transaction;

import com.banking.backend.domain.model.Transaction;
import com.banking.backend.domain.enums.TransactionStatus;
import com.banking.backend.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long transactionId,
        String reference,
        String type,
        BigDecimal amount,
        BigDecimal balance,
        String currency,
        String status,
        String sourceAccountNumber,
        String destinationAccountNumber,
        LocalDateTime createdAt) {

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getReference(),
                transaction.getType().name(),
                transaction.getAmount(),
                getBalance(transaction),
                getCurrency(transaction),
                transaction.getStatus().name(),
                getSourceAccountNumber(transaction),
                getDestinationAccountNumber(transaction),
                transaction.getCreatedAt()
        );
    }

    private static BigDecimal getBalance(Transaction transaction) {
        TransactionType type = transaction.getType();
        if (type == TransactionType.DEPOSIT || type == TransactionType.TRANSFER) {
            if (transaction.getDestinationAccount() != null) {
                return transaction.getDestinationAccount().getBalance();
            }
        }
        if (type == TransactionType.WITHDRAWAL) {
            if (transaction.getSourceAccount() != null) {
                return transaction.getSourceAccount().getBalance();
            }
        }
        return null;
    }

    private static String getCurrency(Transaction transaction) {
        TransactionType type = transaction.getType();
        if (type == TransactionType.DEPOSIT || type == TransactionType.TRANSFER) {
            if (transaction.getDestinationAccount() != null) {
                return transaction.getDestinationAccount().getCurrency();
            }
        }
        if (type == TransactionType.WITHDRAWAL) {
            if (transaction.getSourceAccount() != null) {
                return transaction.getSourceAccount().getCurrency();
            }
        }
        return null;
    }

    private static String getSourceAccountNumber(Transaction transaction) {
        if (transaction.getSourceAccount() != null) {
            return transaction.getSourceAccount().getAccountNumber();
        }
        return null;
    }

    private static String getDestinationAccountNumber(Transaction transaction) {
        if (transaction.getDestinationAccount() != null) {
            return transaction.getDestinationAccount().getAccountNumber();
        }
        return null;
    }
}
