package com.banking.backend.infrastructure.kafka.purchase;

import com.banking.backend.application.purchase.RefundPurchase;
import com.banking.backend.application.purchase.RefundPurchaseCommand;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RefundConfirmationConsumer {

    private final RefundPurchase refundPurchase;

    public RefundConfirmationConsumer(RefundPurchase refundPurchase) {
        this.refundPurchase = refundPurchase;
    }

    @KafkaListener(
        topics = "${app.kafka.topics.purchase-refunds:purchase.refunds}",
        containerFactory = "refundKafkaListenerContainerFactory"
    )
    public void consume(RefundEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Refund event must not be null");
        }
        refundPurchase.execute(new RefundPurchaseCommand(event.eventId(), event.purchaseId(), event.amount()));
    }
}
