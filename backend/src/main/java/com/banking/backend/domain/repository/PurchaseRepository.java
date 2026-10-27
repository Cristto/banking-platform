package com.banking.backend.domain.repository;

import com.banking.backend.domain.model.Purchase;

import java.util.Optional;

public interface PurchaseRepository {

    Purchase save(Purchase purchase);

    Optional<Purchase> findByPurchaseReference(String purchaseReference);

    Optional<Purchase> findById(Long id);
}
