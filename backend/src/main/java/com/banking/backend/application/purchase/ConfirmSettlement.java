package com.banking.backend.application.purchase;

import com.banking.backend.domain.enums.PurchaseStatus;
import com.banking.backend.domain.enums.TransactionStatus;
import com.banking.backend.domain.enums.TransactionType;
import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.model.Purchase;
import com.banking.backend.domain.model.Transaction;
import com.banking.backend.domain.repository.AccountRepository;
import com.banking.backend.domain.repository.PurchaseRepository;
import com.banking.backend.domain.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConfirmSettlement {

    private final PurchaseRepository purchases;
    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public ConfirmSettlement(PurchaseRepository purchases, AccountRepository accounts,
                             TransactionRepository transactions) {
        this.purchases = purchases;
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @Transactional
    public void execute(ConfirmSettlementCommand command) {
        Purchase purchaseSnapshot = purchases.findById(command.purchaseId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Purchase not found: " + command.purchaseId()));

        Account account = accounts.findByIdForUpdate(purchaseSnapshot.getAccount().getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Account not found for purchase: " + command.purchaseId()));
        Purchase purchase = purchases.findByIdForUpdate(command.purchaseId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Purchase not found: " + command.purchaseId()));

        Optional<Transaction> existing = transactions.findByIdempotencyKey(command.eventId());
        if (existing.isPresent()) {
            if (isSettlementForPurchase(existing.get(), purchase)) {
                return;
            }
            throw new IllegalArgumentException("Event ID is already used for a different settlement");
        }

        // A redelivery with a new event ID must not settle or debit the same purchase twice.
        if (purchase.getStatus() == PurchaseStatus.SETTLED) {
            return;
        }
        if (purchase.getStatus() != PurchaseStatus.PENDING) {
            throw new IllegalStateException("Purchase is not pending: " + command.purchaseId());
        }

        if (account.getReservedBalance().compareTo(purchase.getAmount()) < 0
                || account.getBalance().compareTo(purchase.getAmount()) < 0) {
            throw new IllegalStateException("Reserved funds are inconsistent for purchase: " + command.purchaseId());
        }

        account.setReservedBalance(account.getReservedBalance().subtract(purchase.getAmount()));
        account.setBalance(account.getBalance().subtract(purchase.getAmount()));
        accounts.save(account);

        Transaction transaction = new Transaction(UUID.randomUUID().toString(), TransactionType.PURCHASE,
                purchase.getAmount(), TransactionStatus.COMPLETED);
        transaction.setIdempotencyKey(command.eventId());
        transaction.setSourceAccount(account);
        transaction.setDescription("Purchase settlement " + purchase.getId());
        transaction = transactions.save(transaction);

        purchase.setSettlementTransaction(transaction);
        purchase.setStatus(PurchaseStatus.SETTLED);
        purchase.setSettledAt(LocalDateTime.now());
        purchases.save(purchase);
    }

    private boolean isSettlementForPurchase(Transaction transaction, Purchase purchase) {
        return transaction.getType() == TransactionType.PURCHASE
                && purchase.getStatus() == PurchaseStatus.SETTLED
                && purchase.getSettlementTransaction() != null
                && purchase.getSettlementTransaction().getId() != null
                && purchase.getSettlementTransaction().getId().equals(transaction.getId());
    }
}
