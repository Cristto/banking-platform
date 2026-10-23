package com.banking.backend.application.account;

import com.banking.backend.domain.exception.CustomerInactiveException;
import com.banking.backend.domain.exception.CustomerNotFoundException;
import com.banking.backend.domain.enums.AccountStatus;
import com.banking.backend.domain.enums.CustomerStatus;
import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.model.AccountHolder;
import com.banking.backend.domain.model.Customer;
import com.banking.backend.domain.repository.AccountRepository;
import com.banking.backend.domain.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OpenAccountTest {

    private CustomerRepository customers;
    private AccountRepository accounts;
    private OpenAccount openAccount;

    @BeforeEach
    void setUp() {
        customers = mock(CustomerRepository.class);
        accounts = mock(AccountRepository.class);
        openAccount = new OpenAccount(customers, accounts);
    }

    @Test
    void opensAccountWhenCustomerExistsAndIsActive() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        when(customers.findById(1L)).thenReturn(Optional.of(customer));

        Account savedAccount = createAccountWithHolder("ES123456789012345678901234567890", customer, "OWNER");
        when(accounts.saveWithHolder(any(Account.class), eq(customer), eq("OWNER"))).thenReturn(savedAccount);

        Account result = openAccount.execute(new OpenAccountCommand(1L, "OWNER"));

        assertSame(savedAccount, result);
        assertEquals("ES123456789012345678901234567890", result.getAccountNumber());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertEquals("EUR", result.getCurrency());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
        assertEquals(1, result.getAccountHolders().size());
        assertEquals("OWNER", result.getAccountHolders().get(0).getRole());
        verify(customers).findById(1L);
        verify(accounts).saveWithHolder(any(Account.class), eq(customer), eq("OWNER"));
    }

    @Test
    void throwsWhenCustomerDoesNotExist() {
        when(customers.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> openAccount.execute(new OpenAccountCommand(999L, "OWNER")));

        verify(customers).findById(999L);
        verify(accounts, never()).saveWithHolder(any(), any(), any());
    }

    @Test
    void throwsWhenCustomerIsNotActive() {
        Customer inactiveCustomer = new Customer("AB123", "Luis", "Martinez", "luis@example.com");
        inactiveCustomer.setStatus(CustomerStatus.INACTIVE);
        when(customers.findById(2L)).thenReturn(Optional.of(inactiveCustomer));

        assertThrows(CustomerInactiveException.class, () -> openAccount.execute(new OpenAccountCommand(2L, "OWNER")));

        verify(customers).findById(2L);
        verify(accounts, never()).saveWithHolder(any(), any(), any());
    }

    @Test
    void returnsAccountWithZeroBalanceAndDefaultCurrency() {
        Customer customer = new Customer("CD456", "Pedro", "Lopez", "pedro@example.com");
        when(customers.findById(3L)).thenReturn(Optional.of(customer));

        Account savedAccount = createAccountWithHolder("ES987654321098765432109876543210", customer, "OWNER");
        when(accounts.saveWithHolder(any(Account.class), any(Customer.class), eq("OWNER"))).thenReturn(savedAccount);

        Account result = openAccount.execute(new OpenAccountCommand(3L, "OWNER"));

        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertEquals("EUR", result.getCurrency());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getCreatedAt());
    }

    private Account createAccountWithHolder(String accountNumber, Customer customer, String role) {
        Account account = new Account(accountNumber);
        AccountHolder holder = new AccountHolder(account, customer, role);
        account.getAccountHolders().add(holder);
        return account;
    }
}
