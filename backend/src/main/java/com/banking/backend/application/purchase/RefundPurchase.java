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

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefundPurchase {

    private final PurchaseRepository purchases;
    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public RefundPurchase(PurchaseRepository purchases, AccountRepository accounts,
                          TransactionRepository transactions) {
        this.purchases = purchases;
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @Transactional
    public void execute(RefundPurchaseCommand command) {
        Purchase purchaseSnapshot = purchases.findById(command.purchaseId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Purchase not found: " + command.purchaseId()));

        Account account = accounts.findByIdForUpdate(purchaseSnapshot.getAccount().getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Account not found for purchase: " + command.purchaseId()));
        Purchase purchase = purchases.findByIdForUpdate(command.purchaseId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Purchase not found: " + command.purchaseId()));

        // Idempotency: check if a refund transaction with this event ID already exists.
        Optional<Transaction> existing = transactions.findByIdempotencyKey(command.eventId());
        if (existing.isPresent()) {
            if (isRefundForPurchase(existing.get(), purchase)) {
                return;
            }
            throw new IllegalArgumentException("Event ID is already used for a different refund");
        }

        // A redelivery with a new event ID must not refund the same purchase twice.
        if (purchase.getStatus() != PurchaseStatus.SETTLED) {
            throw new IllegalStateException("Purchase is not settled: " + command.purchaseId());
        }

        BigDecimal refundableAmount = purchase.getAmount()
                .subtract(purchase.getRefundedAmount());
        if (command.amount().compareTo(refundableAmount) > 0) {
            throw new IllegalArgumentException(
                    "Refund amount " + command.amount()
                            + " exceeds remaining refundable amount " + refundableAmount
                            + " for purchase: " + command.purchaseId());
        }

        // Credit account balance with refund amount.
        account.setBalance(account.getBalance().add(command.amount()));
        accounts.save(account);

        // Create and save REFUND transaction.
        Transaction transaction = new Transaction(UUID.randomUUID().toString(), TransactionType.REFUND,
                command.amount(), TransactionStatus.COMPLETED);
        transaction.setIdempotencyKey(command.eventId());
        transaction.setDestinationAccount(account);
        transaction.setDescription("Refund for purchase " + purchase.getId());
        transaction = transactions.save(transaction);

        // Update purchase: refunded amount and refund transaction link.
        purchase.setRefundedAmount(purchase.getRefundedAmount().add(command.amount()));
        purchase.setRefundTransaction(transaction);
        purchases.save(purchase);
    }

    private boolean isRefundForPurchase(Transaction transaction, Purchase purchase) {
        return transaction.getType() == TransactionType.REFUND
                && purchase.getStatus() == PurchaseStatus.SETTLED
                && purchase.getRefundTransaction() != null
                && purchase.getRefundTransaction().getId() != null
                && purchase.getRefundTransaction().getId().equals(transaction.getId());
    }
}
