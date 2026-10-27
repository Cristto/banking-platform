package com.banking.backend.infrastructure.persistence.purchase;

import com.banking.backend.domain.model.Purchase;
import com.banking.backend.domain.repository.PurchaseRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import jakarta.persistence.LockModeType;

public interface SpringDataPurchaseRepository extends JpaRepository<Purchase, Long> {

    @Query("select p from Purchase p where p.purchaseReference = :reference")
    Optional<Purchase> findByPurchaseReference(@Param("reference") String reference);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Purchase p where p.id = :id")
    Optional<Purchase> findByIdForUpdate(@Param("id") Long id);
}
