package com.banking.backend.application.purchase;

import com.banking.backend.domain.enums.PurchaseStatus;
import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.model.Purchase;
import com.banking.backend.domain.repository.AccountRepository;
import com.banking.backend.domain.repository.PurchaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
public class RejectSettlement {

    private final PurchaseRepository purchases;
    private final AccountRepository accounts;

    public RejectSettlement(PurchaseRepository purchases, AccountRepository accounts) {
        this.purchases = purchases;
        this.accounts = accounts;
    }

    @Transactional
    public void execute(RejectSettlementCommand command) {
        Purchase purchase = purchases.findByIdForUpdate(command.purchaseId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Purchase not found: " + command.purchaseId()));

        Account account = accounts.findByIdForUpdate(purchase.getAccount().getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Account not found for purchase: " + command.purchaseId()));

        // Idempotency: duplicate rejection events are safe no-ops.
        if (purchase.getStatus() == PurchaseStatus.REJECTED) {
            return;
        }

        // A redelivery with a new event ID must not reject an already settled purchase.
        if (purchase.getStatus() == PurchaseStatus.SETTLED) {
            return;
        }

        // Only PENDING purchases can be rejected.
        if (purchase.getStatus() != PurchaseStatus.PENDING) {
            throw new IllegalStateException("Purchase is not pending: " + command.purchaseId());
        }

        // Release the reserved amount without modifying the account balance.
        account.setReservedBalance(account.getReservedBalance().subtract(purchase.getAmount()));
        accounts.save(account);

        // Update purchase status to REJECTED.
        purchase.setStatus(PurchaseStatus.REJECTED);
        purchase.setRejectedAt(LocalDateTime.now());
        purchases.save(purchase);
    }
}
