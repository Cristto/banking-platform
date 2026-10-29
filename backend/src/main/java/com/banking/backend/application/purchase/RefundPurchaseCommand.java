package com.banking.backend.application.purchase;

import java.math.BigDecimal;

public record RefundPurchaseCommand(
        String eventId,
        Long purchaseId,
        BigDecimal amount) {

    public RefundPurchaseCommand {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Event ID must not be blank");
        }
        if (purchaseId == null || purchaseId <= 0) {
            throw new IllegalArgumentException("Purchase ID must be greater than zero");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Refund amount must be greater than zero");
        }
    }
}
