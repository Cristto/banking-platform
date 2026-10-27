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

class ConfirmSettlementTest {

    private PurchaseRepository purchases;
    private AccountRepository accounts;
    private TransactionRepository transactions;
    private ConfirmSettlement confirmSettlement;
    private Account account;
    private Purchase purchase;

    @BeforeEach
    void setUp() {
        purchases = mock(PurchaseRepository.class);
        accounts = mock(AccountRepository.class);
        transactions = mock(TransactionRepository.class);
        confirmSettlement = new ConfirmSettlement(purchases, accounts, transactions);

        account = Account.openForCustomer(new Customer("AB123", "Ana", "Garcia", "ana@example.com"));
        account.setId(7L);
        account.setBalance(new BigDecimal("1000.00"));
        account.setReservedBalance(new BigDecimal("125.00"));
        purchase = new Purchase(account, "REF-125", "Bookshop", new BigDecimal("125.00"), PurchaseStatus.PENDING);
        purchase.setId(125L);

        when(purchases.findById(125L)).thenReturn(Optional.of(purchase));
        when(purchases.findByIdForUpdate(125L)).thenReturn(Optional.of(purchase));
        when(accounts.findByIdForUpdate(7L)).thenReturn(Optional.of(account));
        when(transactions.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(transactions.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId(99L);
            return transaction;
        });
    }

    @Test
    void debitsBalanceReleasesReservationAndLinksPurchaseTransaction() {
        confirmSettlement.execute(new ConfirmSettlementCommand("event-1", 125L));

        assertEquals(new BigDecimal("875.00"), account.getBalance());
        assertEquals(new BigDecimal("0.00"), account.getReservedBalance());
        assertEquals(PurchaseStatus.SETTLED, purchase.getStatus());
        assertNotNull(purchase.getSettledAt());
        assertSame(account, purchase.getSettlementTransaction().getSourceAccount());
        assertEquals(TransactionType.PURCHASE, purchase.getSettlementTransaction().getType());
        assertEquals("event-1", purchase.getSettlementTransaction().getIdempotencyKey());
        assertEquals(TransactionStatus.COMPLETED, purchase.getSettlementTransaction().getStatus());
        verify(accounts).save(account);
        verify(transactions).save(any(Transaction.class));
        verify(purchases).save(purchase);
    }

    @Test
    void duplicateEventDoesNotDebitOrCreateAnotherTransaction() {
        Transaction existing = new Transaction("settlement-ref", TransactionType.PURCHASE,
                new BigDecimal("125.00"), TransactionStatus.COMPLETED);
        existing.setId(99L);
        existing.setIdempotencyKey("event-1");
        purchase.setStatus(PurchaseStatus.SETTLED);
        purchase.setSettlementTransaction(existing);
        when(transactions.findByIdempotencyKey("event-1")).thenReturn(Optional.of(existing));

        confirmSettlement.execute(new ConfirmSettlementCommand("event-1", 125L));

        assertEquals(new BigDecimal("1000.00"), account.getBalance());
        assertEquals(new BigDecimal("125.00"), account.getReservedBalance());
        verify(accounts, never()).save(any());
        verify(transactions, never()).save(any());
    }

    @Test
    void alreadySettledPurchaseWithDifferentEventDoesNotSettleTwice() {
        purchase.setStatus(PurchaseStatus.SETTLED);
        confirmSettlement.execute(new ConfirmSettlementCommand("event-2", 125L));

        assertEquals(new BigDecimal("1000.00"), account.getBalance());
        assertEquals(new BigDecimal("125.00"), account.getReservedBalance());
        verify(transactions, never()).save(any());
    }

    @Test
    void rejectsPurchaseThatIsNotPending() {
        purchase.setStatus(PurchaseStatus.DECLINED);

        assertThrows(IllegalStateException.class,
                () -> confirmSettlement.execute(new ConfirmSettlementCommand("event-1", 125L)));

        verify(accounts, never()).save(any());
        verify(transactions, never()).save(any());
    }
}
