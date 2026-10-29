package com.banking.backend.infrastructure.persistence.purchase;

import com.banking.backend.domain.model.Purchase;
import com.banking.backend.domain.repository.PurchaseRepository;
import org.springframework.stereotype.Component;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Component
public class JpaPurchaseRepository implements PurchaseRepository {

    private final SpringDataPurchaseRepository springDataRepository;

    public JpaPurchaseRepository(SpringDataPurchaseRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Purchase save(Purchase purchase) {
        return springDataRepository.save(purchase);
    }

    @Override
    public Optional<Purchase> findByPurchaseReference(String purchaseReference) {
        return springDataRepository.findByPurchaseReference(purchaseReference);
    }

    @Override
    public Optional<Purchase> findById(Long id) {
        return springDataRepository.findById(id);
    }

    @Override
    public Optional<Purchase> findByIdForUpdate(Long id) {
        return springDataRepository.findByIdForUpdate(id);
    }

    @Override
    public Page<Purchase> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable) {
        return springDataRepository.findByAccount_IdOrderByCreatedAtDesc(accountId, pageable);
    }
}
