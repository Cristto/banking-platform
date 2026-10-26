package com.banking.backend.domain.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(Long accountId, BigDecimal requestedAmount, BigDecimal currentBalance) {
        super("Account " + accountId + " has insufficient funds: requested " + requestedAmount
                + ", available " + currentBalance);
    }
}
