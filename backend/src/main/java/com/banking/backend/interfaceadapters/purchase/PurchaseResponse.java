package com.banking.backend.interfaceadapters.purchase;

import com.banking.backend.domain.model.Purchase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PurchaseResponse(
        Long purchaseId,
        String purchaseReference,
        String merchantName,
        BigDecimal amount,
        BigDecimal availableBalance,
        String currency,
        String status,
        LocalDateTime authorizedAt,
        LocalDateTime settledAt,
        LocalDateTime rejectedAt) {

    public static PurchaseResponse from(Purchase purchase, BigDecimal availableBalance) {
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getPurchaseReference(),
                purchase.getMerchantName(),
                purchase.getAmount(),
                availableBalance,
                purchase.getAccount().getCurrency(),
                purchase.getStatus().name(),
                purchase.getAuthorizedAt(),
                purchase.getSettledAt(),
                purchase.getRejectedAt()
        );
    }
}
