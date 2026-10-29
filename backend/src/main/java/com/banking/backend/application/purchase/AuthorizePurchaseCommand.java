package com.banking.backend.application.purchase;

import java.math.BigDecimal;

public record AuthorizePurchaseCommand(
        Long accountId,
        String purchaseReference,
        String merchantName,
        BigDecimal amount) {

    public AuthorizePurchaseCommand {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID must not be null");
        }
        if (purchaseReference == null || purchaseReference.isBlank()) {
            throw new IllegalArgumentException("Purchase reference must not be blank");
        }
        if (merchantName == null || merchantName.isBlank()) {
            throw new IllegalArgumentException("Merchant name must not be blank");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}
