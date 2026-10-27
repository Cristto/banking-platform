package com.banking.backend.infrastructure.persistence.purchase;

import com.banking.backend.domain.model.Purchase;
import com.banking.backend.domain.repository.PurchaseRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataPurchaseRepository extends JpaRepository<Purchase, Long> {

    @Query("select p from Purchase p where p.purchaseReference = :reference")
    Optional<Purchase> findByPurchaseReference(@Param("reference") String reference);
}
