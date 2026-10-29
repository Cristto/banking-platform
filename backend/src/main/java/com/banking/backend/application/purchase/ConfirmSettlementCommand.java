package com.banking.backend.application.purchase;

public record ConfirmSettlementCommand(String eventId, Long purchaseId) {

    public ConfirmSettlementCommand {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Event ID must not be blank");
        }
        if (eventId.length() > 100) {
            throw new IllegalArgumentException("Event ID must not exceed 100 characters");
        }
        if (purchaseId == null || purchaseId <= 0) {
            throw new IllegalArgumentException("Purchase ID must be greater than zero");
        }
    }
}
