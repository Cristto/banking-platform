package com.banking.backend.interfaceadapters.purchase;

import java.math.BigDecimal;

public record PurchaseRequest(
        String purchaseReference,
        String merchantName,
        BigDecimal amount) {
}
