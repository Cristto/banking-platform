package com.banking.backend.infrastructure.persistence.purchase;

import com.banking.backend.domain.model.Purchase;
import com.banking.backend.domain.repository.PurchaseRepository;
import org.springframework.stereotype.Component;

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
}
