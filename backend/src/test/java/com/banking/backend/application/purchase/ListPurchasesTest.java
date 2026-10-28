package com.banking.backend.application.purchase;

import com.banking.backend.domain.exception.AccountNotFoundException;
import com.banking.backend.domain.enums.PurchaseStatus;
import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.model.Customer;
import com.banking.backend.domain.model.Purchase;
import com.banking.backend.domain.repository.AccountRepository;
import com.banking.backend.domain.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ListPurchasesTest {

    private AccountRepository accounts;
    private PurchaseRepository purchases;
    private ListPurchases listPurchases;
    private Account account;

    @BeforeEach
    void setUp() {
        accounts = mock(AccountRepository.class);
        purchases = mock(PurchaseRepository.class);
        listPurchases = new ListPurchases(accounts, purchases);

        account = Account.openForCustomer(new Customer("AB123", "Ana", "Garcia", "ana@example.com"));
        account.setId(1L);
        account.setBalance(new BigDecimal("1000.00"));
        account.setReservedBalance(new BigDecimal("125.00"));
    }

    @Test
    void returnsEmptyPageWhenAccountHasNoPurchases() {
        when(accounts.findById(1L)).thenReturn(Optional.of(account));
        when(purchases.findByAccountIdOrderByCreatedAtDesc(1L, PageRequest.of(0, 10)))
                .thenReturn(Page.empty());

        Page<com.banking.backend.interfaceadapters.purchase.PurchaseResponse> result =
                listPurchases.execute(new ListPurchasesCommand(1L, PageRequest.of(0, 10)));

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(accounts).findById(1L);
        verify(purchases).findByAccountIdOrderByCreatedAtDesc(1L, PageRequest.of(0, 10));
    }

    @Test
    void returnsPurchasesForAccountOrderedByCreatedAtDesc() {
        Purchase pending = createPurchase(100L, "REF-001", "Bookshop", new BigDecimal("50.00"),
                PurchaseStatus.PENDING, null, null, null);
        Purchase settled = createPurchase(200L, "REF-002", "Electronics", new BigDecimal("200.00"),
                PurchaseStatus.SETTLED, LocalDateTime.now(), LocalDateTime.now(), null);
        Purchase rejected = createPurchase(300L, "REF-003", "Clothing", new BigDecimal("30.00"),
                PurchaseStatus.REJECTED, null, null, LocalDateTime.now());

        when(accounts.findById(1L)).thenReturn(Optional.of(account));
        when(purchases.findByAccountIdOrderByCreatedAtDesc(1L, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(pending, settled, rejected)));

        Page<com.banking.backend.interfaceadapters.purchase.PurchaseResponse> result =
                listPurchases.execute(new ListPurchasesCommand(1L, PageRequest.of(0, 10)));

        assertEquals(3, result.getTotalElements());
        var purchaseIds = result.getContent().stream()
                .map(com.banking.backend.interfaceadapters.purchase.PurchaseResponse::purchaseId)
                .toList();
        assertTrue(purchaseIds.contains(100L));
        assertTrue(purchaseIds.contains(200L));
        assertTrue(purchaseIds.contains(300L));
        verify(accounts).findById(1L);
        verify(purchases).findByAccountIdOrderByCreatedAtDesc(1L, PageRequest.of(0, 10));
    }

    @Test
    void throwsWhenAccountDoesNotExist() {
        when(accounts.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> listPurchases.execute(new ListPurchasesCommand(999L, PageRequest.of(0, 10))));

        verify(accounts).findById(999L);
        verify(purchases, never()).findByAccountIdOrderByCreatedAtDesc(anyLong(), any());
    }

    @Test
    void includesSettledAtAndRejectedAtInResponse() {
        LocalDateTime settledTime = LocalDateTime.of(2026, 10, 15, 10, 0);
        LocalDateTime rejectedTime = LocalDateTime.of(2026, 10, 16, 11, 0);

        Purchase settled = createPurchase(1L, "REF-SETTLED", "Store", new BigDecimal("10.00"),
                PurchaseStatus.SETTLED, null, settledTime, null);
        Purchase rejected = createPurchase(2L, "REF-REJECTED", "Shop", new BigDecimal("20.00"),
                PurchaseStatus.REJECTED, null, null, rejectedTime);

        when(accounts.findById(1L)).thenReturn(Optional.of(account));
        when(purchases.findByAccountIdOrderByCreatedAtDesc(1L, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(settled, rejected)));

        Page<com.banking.backend.interfaceadapters.purchase.PurchaseResponse> result =
                listPurchases.execute(new ListPurchasesCommand(1L, PageRequest.of(0, 10)));

        assertEquals(settledTime, result.getContent().get(0).settledAt());
        assertNull(result.getContent().get(0).rejectedAt());
        assertNull(result.getContent().get(1).settledAt());
        assertEquals(rejectedTime, result.getContent().get(1).rejectedAt());
    }

    @Test
    void nullAccountIdThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> listPurchases.execute(new ListPurchasesCommand(null, PageRequest.of(0, 10))));
    }

    @Test
    void nullPageableThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> listPurchases.execute(new ListPurchasesCommand(1L, null)));
    }

    private Purchase createPurchase(Long id, String reference, String merchant,
                                    BigDecimal amount, PurchaseStatus status,
                                    LocalDateTime authorizedAt,
                                    LocalDateTime settledAt,
                                    LocalDateTime rejectedAt) {
        Purchase purchase = new Purchase(account, reference, merchant, amount, status);
        purchase.setId(id);
        purchase.setAuthorizedAt(authorizedAt);
        purchase.setSettledAt(settledAt);
        purchase.setRejectedAt(rejectedAt);
        return purchase;
    }
}
