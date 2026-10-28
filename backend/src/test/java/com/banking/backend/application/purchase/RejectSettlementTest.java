package com.banking.backend.application.purchase;

import com.banking.backend.domain.enums.PurchaseStatus;
import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.model.Customer;
import com.banking.backend.domain.model.Purchase;
import com.banking.backend.domain.model.Transaction;
import com.banking.backend.domain.repository.AccountRepository;
import com.banking.backend.domain.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RejectSettlementTest {

    private PurchaseRepository purchases;
    private AccountRepository accounts;
    private RejectSettlement rejectSettlement;
    private Account account;
    private Purchase purchase;

    @BeforeEach
    void setUp() {
        purchases = mock(PurchaseRepository.class);
        accounts = mock(AccountRepository.class);
        rejectSettlement = new RejectSettlement(purchases, accounts);

        account = Account.openForCustomer(new Customer("AB123", "Ana", "Garcia", "ana@example.com"));
        account.setId(7L);
        account.setBalance(new BigDecimal("1000.00"));
        account.setReservedBalance(new BigDecimal("125.00"));
        purchase = new Purchase(account, "REF-125", "Bookshop", new BigDecimal("125.00"), PurchaseStatus.PENDING);
        purchase.setId(125L);

        when(purchases.findById(125L)).thenReturn(Optional.of(purchase));
        when(purchases.findByIdForUpdate(125L)).thenReturn(Optional.of(purchase));
        when(accounts.findByIdForUpdate(7L)).thenReturn(Optional.of(account));
    }

    @Test
    void releasesReservationAndMarksPurchaseAsRejected() {
        rejectSettlement.execute(new RejectSettlementCommand("event-1", 125L));

        assertEquals(new BigDecimal("1000.00"), account.getBalance());
        assertEquals(new BigDecimal("0.00"), account.getReservedBalance());
        assertEquals(PurchaseStatus.REJECTED, purchase.getStatus());
        assertNotNull(purchase.getRejectedAt());
        verify(accounts).save(account);
        verify(purchases).save(purchase);
        verifyNoMoreInteractions(purchases);
    }

    @Test
    void duplicateEventIsIdempotent() {
        purchase.setStatus(PurchaseStatus.REJECTED);
        purchase.setRejectedAt(java.time.LocalDateTime.now());

        rejectSettlement.execute(new RejectSettlementCommand("event-1", 125L));

        assertEquals(new BigDecimal("1000.00"), account.getBalance());
        assertEquals(new BigDecimal("0.00"), account.getReservedBalance());
        assertEquals(PurchaseStatus.REJECTED, purchase.getStatus());
        verify(accounts, never()).save(any());
        verify(purchases, never()).save(any());
    }

    @Test
    void alreadySettledPurchaseWithDifferentEventIsNoOp() {
        purchase.setStatus(PurchaseStatus.SETTLED);

        rejectSettlement.execute(new RejectSettlementCommand("event-2", 125L));

        assertEquals(new BigDecimal("1000.00"), account.getBalance());
        assertEquals(new BigDecimal("125.00"), account.getReservedBalance());
        assertEquals(PurchaseStatus.SETTLED, purchase.getStatus());
        verify(accounts, never()).save(any());
        verify(purchases, never()).save(any());
    }

    @Test
    void rejectsPurchaseThatIsNotPending() {
        purchase.setStatus(PurchaseStatus.DECLINED);

        assertThrows(IllegalStateException.class,
                () -> rejectSettlement.execute(new RejectSettlementCommand("event-1", 125L)));

        assertEquals(PurchaseStatus.DECLINED, purchase.getStatus());
        assertEquals(new BigDecimal("1000.00"), account.getBalance());
        assertEquals(new BigDecimal("125.00"), account.getReservedBalance());
        verify(accounts, never()).save(any());
        verify(purchases, never()).save(any());
    }

    @Test
    void alreadySettledPurchaseDoesNotCreatePurchaseTransaction() {
        Transaction existing = new Transaction("settlement-ref",
                com.banking.backend.domain.enums.TransactionType.PURCHASE,
                new BigDecimal("125.00"),
                com.banking.backend.domain.enums.TransactionStatus.COMPLETED);
        existing.setId(99L);
        purchase.setStatus(PurchaseStatus.SETTLED);
        purchase.setSettlementTransaction(existing);

        rejectSettlement.execute(new RejectSettlementCommand("event-1", 125L));

        assertEquals(PurchaseStatus.SETTLED, purchase.getStatus());
        assertNull(purchase.getSettlementTransaction());
    }
}
