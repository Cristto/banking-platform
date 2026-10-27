package com.banking.backend.infrastructure.kafka.purchase;

import com.banking.backend.application.purchase.ConfirmSettlement;
import com.banking.backend.application.purchase.ConfirmSettlementCommand;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SettlementConfirmationConsumer {

    private final ConfirmSettlement confirmSettlement;

    public SettlementConfirmationConsumer(ConfirmSettlement confirmSettlement) {
        this.confirmSettlement = confirmSettlement;
    }

    @KafkaListener(topics = "${app.kafka.topics.purchase-settlements:purchase.settlements}")
    public void consume(SettlementEvent event) {
        if (event == null || event.action() != SettlementAction.CONFIRM) {
            throw new IllegalArgumentException("Settlement event action must be CONFIRM");
        }
        confirmSettlement.execute(new ConfirmSettlementCommand(event.eventId(), event.purchaseId()));
    }
}
