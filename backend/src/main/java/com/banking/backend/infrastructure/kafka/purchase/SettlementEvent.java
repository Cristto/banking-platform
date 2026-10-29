package com.banking.backend.infrastructure.kafka.purchase;

public record SettlementEvent(String eventId, Long purchaseId, SettlementAction action) {
}
