package com.banking.backend.application.purchase;

public record RejectSettlementCommand(String eventId, Long purchaseId) {

    public RejectSettlementCommand {
        if (eventId == null || eventId.isBlank() || eventId.length() > 100) {
            throw new IllegalArgumentException("eventId must be a non-blank string up to 100 characters");
        }
        if (purchaseId == null || purchaseId <= 0) {
            throw new IllegalArgumentException("purchaseId must be greater than zero");
        }
    }
}
