package com.banking.backend.infrastructure.kafka.purchase;

import com.banking.backend.application.purchase.ConfirmSettlement;
import com.banking.backend.application.purchase.ConfirmSettlementCommand;
import com.banking.backend.application.purchase.RejectSettlement;
import com.banking.backend.application.purchase.RejectSettlementCommand;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SettlementConfirmationConsumer {

    private final ConfirmSettlement confirmSettlement;
    private final RejectSettlement rejectSettlement;

    public SettlementConfirmationConsumer(ConfirmSettlement confirmSettlement,
                                          RejectSettlement rejectSettlement) {
        this.confirmSettlement = confirmSettlement;
        this.rejectSettlement = rejectSettlement;
    }

    @KafkaListener(topics = "${app.kafka.topics.purchase-settlements:purchase.settlements}")
    public void consume(SettlementEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Settlement event must not be null");
        }
        switch (event.action()) {
            case CONFIRM -> confirmSettlement.execute(
                    new ConfirmSettlementCommand(event.eventId(), event.purchaseId()));
            case REJECT -> rejectSettlement.execute(
                    new RejectSettlementCommand(event.eventId(), event.purchaseId()));
        }
    }
}
