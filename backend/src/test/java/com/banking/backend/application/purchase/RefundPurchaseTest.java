package com.banking.backend.application.purchase;

import com.banking.backend.domain.enums.PurchaseStatus;
import com.banking.backend.domain.enums.TransactionStatus;
import com.banking.backend.domain.enums.TransactionType;
import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.model.Customer;
import com.banking.backend.domain.model.Purchase;
import com.banking.backend.domain.model.Transaction;
import com.banking.backend.domain.repository.AccountRepository;
import com.banking.backend.domain.repository.PurchaseRepository;
import com.banking.backend.domain.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RefundPurchaseTest {

    private PurchaseRepository purchases;
    private AccountRepository accounts;
    private TransactionRepository transactions;
    private RefundPurchase refundPurchase;
    private Account account;
    private Purchase settledPurchase;

    @BeforeEach
    void setUp() {
        purchases = mock(PurchaseRepository.class);
        accounts = mock(AccountRepository.class);
        transactions = mock(TransactionRepository.class);
        refundPurchase = new RefundPurchase(purchases, accounts, transactions);

        account = Account.openForCustomer(new Customer("AB123", "Ana", "Garcia", "ana@example.com"));
        account.setId(7L);
        account.setBalance(new BigDecimal("1000.00"));
        account.setReservedBalance(BigDecimal.ZERO);

        // Create a SETTLED purchase with a settlement transaction.
        settledPurchase = new Purchase(account, "REF-200", "Electronics", new BigDecimal("200.00"),
                PurchaseStatus.SETTLED);
        settledPurchase.setId(200L);
        settledPurchase.setSettledAt(java.time.LocalDateTime.now());

        Transaction settlement = new Transaction("settlement-ref", TransactionType.PURCHASE,
                new BigDecimal("200.00"), TransactionStatus.COMPLETED);
        settlement.setId(50L);
        settlement.setIdempotencyKey("settlement-event-1");
        settledPurchase.setSettlementTransaction(settlement);

        when(purchases.findById(200L)).thenReturn(Optional.of(settledPurchase));
        when(purchases.findByIdForUpdate(200L)).thenReturn(Optional.of(settledPurchase));
        when(accounts.findByIdForUpdate(7L)).thenReturn(Optional.of(account));
        when(transactions.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(transactions.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId(100L);
            return transaction;
        });
    }

    @Test
    void fullRefundCreditsBalanceAndLinksPurchaseTransaction() {
        refundPurchase.execute(new RefundPurchaseCommand("refund-event-1", 200L,
                new BigDecimal("200.00")));

        assertEquals(new BigDecimal("1200.00"), account.getBalance());
        assertEquals(new BigDecimal("200.00"), settledPurchase.getRefundedAmount());
        assertEquals(PurchaseStatus.SETTLED, settledPurchase.getStatus());
        assertNotNull(settledPurchase.getRefundTransaction());
        assertEquals(TransactionType.REFUND, settledPurchase.getRefundTransaction().getType());
        assertEquals("refund-event-1", settledPurchase.getRefundTransaction().getIdempotencyKey());
        assertEquals(TransactionStatus.COMPLETED, settledPurchase.getRefundTransaction().getStatus());
        assertSame(account, settledPurchase.getRefundTransaction().getDestinationAccount());
        assertEquals("Refund for purchase 200", settledPurchase.getRefundTransaction().getDescription());
        verify(accounts).save(account);
        verify(transactions).save(any(Transaction.class));
        verify(purchases).save(settledPurchase);
    }

    @Test
    void partialRefundCreditsPartialAmount() {
        refundPurchase.execute(new RefundPurchaseCommand("refund-event-1", 200L,
                new BigDecimal("75.00")));

        assertEquals(new BigDecimal("1075.00"), account.getBalance());
        assertEquals(new BigDecimal("75.00"), settledPurchase.getRefundedAmount());
        assertNotNull(settledPurchase.getRefundTransaction());
        assertEquals(TransactionType.REFUND, settledPurchase.getRefundTransaction().getType());
        assertEquals(new BigDecimal("75.00"), settledPurchase.getRefundTransaction().getAmount());
        verify(transactions).save(any(Transaction.class));
        verify(purchases).save(settledPurchase);
    }

    @Test
    void duplicateEventDoesNotCreditOrCreateAnotherTransaction() {
        Transaction existing = new Transaction("refund-ref", TransactionType.REFUND,
                new BigDecimal("200.00"), TransactionStatus.COMPLETED);
        existing.setId(100L);
        existing.setIdempotencyKey("refund-event-1");
        settledPurchase.setRefundTransaction(existing);
        when(transactions.findByIdempotencyKey("refund-event-1")).thenReturn(Optional.of(existing));

        refundPurchase.execute(new RefundPurchaseCommand("refund-event-1", 200L,
                new BigDecimal("200.00")));

        assertEquals(new BigDecimal("1000.00"), account.getBalance());
        assertEquals(BigDecimal.ZERO, settledPurchase.getRefundedAmount());
        verify(accounts, never()).save(any());
        verify(transactions, never()).save(any());
    }

    @Test
    void alreadyFullyRefundedPurchaseWithDifferentEventDoesNotRefundTwice() {
        settledPurchase.setRefundedAmount(new BigDecimal("200.00"));

        assertThrows(IllegalArgumentException.class,
                () -> refundPurchase.execute(new RefundPurchaseCommand("refund-event-2", 200L,
                        new BigDecimal("50.00"))));

        assertEquals(new BigDecimal("1000.00"), account.getBalance());
        verify(transactions, never()).save(any());
    }

    @Test
    void refundExceedingRemainingAmountThrows() {
        // Purchase amount is 200, already partially refunded 50, so refundable = 150
        settledPurchase.setRefundedAmount(new BigDecimal("50.00"));

        assertThrows(IllegalArgumentException.class,
                () -> refundPurchase.execute(new RefundPurchaseCommand("refund-event-2", 200L,
                        new BigDecimal("151.00"))));

        assertEquals(new BigDecimal("1000.00"), account.getBalance());
        verify(transactions, never()).save(any());
    }

    @Test
    void rejectsNonSettledPurchase() {
        Purchase pendingPurchase = new Purchase(account, "REF-999", "Shop", new BigDecimal("50.00"),
                PurchaseStatus.AUTHORIZED);
        pendingPurchase.setId(999L);

        when(purchases.findById(999L)).thenReturn(Optional.of(pendingPurchase));
        when(purchases.findByIdForUpdate(999L)).thenReturn(Optional.of(pendingPurchase));

        assertThrows(IllegalStateException.class,
                () -> refundPurchase.execute(new RefundPurchaseCommand("refund-event-3", 999L,
                        new BigDecimal("50.00"))));

        verify(transactions, never()).save(any());
    }

    @Test
    void throwsWhenPurchaseNotFound() {
        when(purchases.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> refundPurchase.execute(new RefundPurchaseCommand("refund-event-1", 999L,
                        new BigDecimal("50.00"))));
    }

    @Test
    void throwsWhenAccountNotFound() {
        when(purchases.findById(200L)).thenReturn(Optional.of(settledPurchase));
        when(accounts.findByIdForUpdate(7L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> refundPurchase.execute(new RefundPurchaseCommand("refund-event-1", 200L,
                        new BigDecimal("50.00"))));
    }

    @Test
    void nullEventIdThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> refundPurchase.execute(new RefundPurchaseCommand(null, 200L,
                        new BigDecimal("50.00"))));
    }

    @Test
    void nullPurchaseIdThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> refundPurchase.execute(new RefundPurchaseCommand("event-1", null,
                        new BigDecimal("50.00"))));
    }

    @Test
    void zeroAmountThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> refundPurchase.execute(new RefundPurchaseCommand("event-1", 200L, BigDecimal.ZERO)));
    }

    @Test
    void negativeAmountThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> refundPurchase.execute(new RefundPurchaseCommand("event-1", 200L,
                        new BigDecimal("-10.00"))));
    }
}
