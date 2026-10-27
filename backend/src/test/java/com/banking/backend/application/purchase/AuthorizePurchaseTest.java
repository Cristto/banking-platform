package com.banking.backend.application.purchase;

import com.banking.backend.domain.enums.AccountStatus;
import com.banking.backend.domain.enums.PurchaseStatus;
import com.banking.backend.domain.exception.AccountInactiveException;
import com.banking.backend.domain.exception.AccountNotFoundException;
import com.banking.backend.domain.exception.InsufficientFundsException;
import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.model.Customer;
import com.banking.backend.domain.model.Purchase;
import com.banking.backend.domain.repository.AccountRepository;
import com.banking.backend.domain.repository.PurchaseRepository;
import com.banking.backend.interfaceadapters.purchase.PurchaseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthorizePurchaseTest {

    private AccountRepository accounts;
    private PurchaseRepository purchases;
    private AuthorizePurchase authorizePurchase;

    @BeforeEach
    void setUp() {
        accounts = mock(AccountRepository.class);
        purchases = mock(PurchaseRepository.class);
        authorizePurchase = new AuthorizePurchase(accounts, purchases);
    }

    @Test
    void authorizesPurchaseWhenAccountExistsAndHasSufficientFunds() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account account = createAccount("ES123456789012345678901234567890", customer);
        account.setBalance(new BigDecimal("1000.00"));
        account.setReservedBalance(BigDecimal.ZERO);

        when(accounts.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        when(purchases.findByPurchaseReference("REF-001")).thenReturn(Optional.empty());
        when(purchases.save(any(Purchase.class))).thenAnswer(invocation -> {
            Purchase purchase = invocation.getArgument(0);
            purchase.setId(1L);
            return purchase;
        });

        PurchaseResponse response = authorizePurchase.execute(
                new AuthorizePurchaseCommand(1L, "REF-001", "Amazon", new BigDecimal("100.00"))
        );

        assertNotNull(response);
        assertEquals(1L, response.purchaseId());
        assertEquals("REF-001", response.purchaseReference());
        assertEquals("Amazon", response.merchantName());
        assertEquals(new BigDecimal("100.00"), response.amount());
        assertEquals(new BigDecimal("900.00"), response.availableBalance());
        assertEquals("EUR", response.currency());
        assertEquals("AUTHORIZED", response.status());
        assertNotNull(response.authorizedAt());

        verify(accounts).findByIdForUpdate(1L);
        verify(purchases).findByPurchaseReference("REF-001");
        verify(purchases).save(any(Purchase.class));
        verify(accounts).save(account);
    }

    @Test
    void returnsExistingPurchaseWhenReferenceAlreadyExists() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account account = createAccount("ES123456789012345678901234567890", customer);
        account.setBalance(new BigDecimal("1000.00"));
        account.setReservedBalance(new BigDecimal("100.00"));

        Purchase existingPurchase = createPurchase(account, "REF-001", "Amazon", new BigDecimal("100.00"));
        existingPurchase.setId(1L);

        when(accounts.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        when(purchases.findByPurchaseReference("REF-001")).thenReturn(Optional.of(existingPurchase));

        PurchaseResponse response = authorizePurchase.execute(
                new AuthorizePurchaseCommand(1L, "REF-001", "Amazon", new BigDecimal("100.00"))
        );

        assertNotNull(response);
        assertEquals(1L, response.purchaseId());
        assertEquals("AUTHORIZED", response.status());

        verify(accounts).findByIdForUpdate(1L);
        verify(purchases).findByPurchaseReference("REF-001");
        verify(purchases, never()).save(any());
        verify(accounts, never()).save(any());
    }

    @Test
    void throwsWhenAccountNotFound() {
        when(accounts.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
                authorizePurchase.execute(new AuthorizePurchaseCommand(999L, "REF-001", "Amazon", new BigDecimal("100.00"))))
        ;

        verify(accounts).findByIdForUpdate(999L);
        verify(purchases, never()).findByPurchaseReference(any());
        verify(purchases, never()).save(any());
    }

    @Test
    void throwsWhenAccountIsNotActive() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account account = createAccount("ES123456789012345678901234567890", customer);
        account.setBalance(new BigDecimal("1000.00"));
        account.setStatus(AccountStatus.BLOCKED);

        when(accounts.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        when(purchases.findByPurchaseReference("REF-001")).thenReturn(Optional.empty());

        assertThrows(AccountInactiveException.class, () ->
                authorizePurchase.execute(new AuthorizePurchaseCommand(1L, "REF-001", "Amazon", new BigDecimal("100.00"))))
        ;

        verify(accounts).findByIdForUpdate(1L);
        verify(purchases).findByPurchaseReference("REF-001");
        verify(purchases, never()).save(any());
    }

    @Test
    void throwsWhenInsufficientAvailableFunds() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account account = createAccount("ES123456789012345678901234567890", customer);
        account.setBalance(new BigDecimal("100.00"));
        account.setReservedBalance(new BigDecimal("80.00"));

        when(accounts.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        when(purchases.findByPurchaseReference("REF-001")).thenReturn(Optional.empty());

        assertThrows(InsufficientFundsException.class, () ->
                authorizePurchase.execute(new AuthorizePurchaseCommand(1L, "REF-001", "Amazon", new BigDecimal("50.00"))))
        ;

        verify(accounts).findByIdForUpdate(1L);
        verify(purchases).findByPurchaseReference("REF-001");
        verify(purchases, never()).save(any());
    }

    @Test
    void reservesFundsWhenPurchaseIsAuthorized() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account account = createAccount("ES123456789012345678901234567890", customer);
        account.setBalance(new BigDecimal("1000.00"));
        account.setReservedBalance(BigDecimal.ZERO);

        when(accounts.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        when(purchases.findByPurchaseReference("REF-001")).thenReturn(Optional.empty());
        when(purchases.save(any(Purchase.class))).thenAnswer(invocation -> {
            Purchase purchase = invocation.getArgument(0);
            purchase.setId(1L);
            return purchase;
        });

        PurchaseResponse response = authorizePurchase.execute(
                new AuthorizePurchaseCommand(1L, "REF-001", "Amazon", new BigDecimal("200.00"))
        );

        assertEquals(new BigDecimal("800.00"), response.availableBalance());
        assertEquals(new BigDecimal("200.00"), account.getReservedBalance());

        verify(accounts).save(account);
    }

    private Account createAccount(String accountNumber, Customer customer) {
        Account account = Account.openForCustomer(customer);
        account.setId(1L);
        return account;
    }

    private Purchase createPurchase(Account account, String reference, String merchant, BigDecimal amount) {
        Purchase purchase = new Purchase(account, reference, merchant, amount, PurchaseStatus.AUTHORIZED);
        return purchase;
    }
}
