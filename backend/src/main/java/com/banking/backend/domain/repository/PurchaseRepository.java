package com.banking.backend.domain.repository;

import com.banking.backend.domain.model.Purchase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PurchaseRepository {

    Purchase save(Purchase purchase);

    Optional<Purchase> findByPurchaseReference(String purchaseReference);

    Optional<Purchase> findById(Long id);

    Optional<Purchase> findByIdForUpdate(Long id);

    Page<Purchase> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);
}
