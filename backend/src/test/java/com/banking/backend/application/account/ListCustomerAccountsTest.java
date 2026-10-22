package com.banking.backend.application.account;

import com.banking.backend.domain.exception.CustomerNotFoundException;
import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.model.AccountHolder;
import com.banking.backend.domain.model.Customer;
import com.banking.backend.domain.repository.AccountRepository;
import com.banking.backend.domain.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListCustomerAccountsTest {

    private CustomerRepository customers;
    private AccountRepository accounts;
    private ListCustomerAccounts listAccounts;

    @BeforeEach
    void setUp() {
        customers = mock(CustomerRepository.class);
        accounts = mock(AccountRepository.class);
        listAccounts = new ListCustomerAccounts(customers, accounts);
    }

    @Test
    void returnsEmptyListWhenCustomerHasNoAccounts() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        when(customers.existsById(1L)).thenReturn(true);
        when(accounts.findByCustomerId(1L)).thenReturn(List.of());

        List<Account> result = listAccounts.execute(new ListCustomerAccountsCommand(1L));

        assertTrue(result.isEmpty());
        verify(customers).existsById(1L);
        verify(accounts).findByCustomerId(1L);
    }

    @Test
    void returnsAllAccountsBelongingToCustomer() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account account1 = createAccountWithHolder("ES111111111111111111111111111111", customer, "OWNER");
        Account account2 = createAccountWithHolder("ES222222222222222222222222222222", customer, "OWNER");
        when(customers.existsById(2L)).thenReturn(true);
        when(accounts.findByCustomerId(2L)).thenReturn(List.of(account1, account2));

        List<Account> result = listAccounts.execute(new ListCustomerAccountsCommand(2L));

        assertEquals(2, result.size());
        assertEquals("ES111111111111111111111111111111", result.get(0).getAccountNumber());
        assertEquals("ES222222222222222222222222222222", result.get(1).getAccountNumber());
        assertEquals(BigDecimal.ZERO, result.get(0).getBalance());
        assertEquals("EUR", result.get(0).getCurrency());
        verify(customers).existsById(2L);
        verify(accounts).findByCustomerId(2L);
    }

    @Test
    void throwsWhenCustomerDoesNotExist() {
        when(customers.existsById(999L)).thenReturn(false);

        assertThrows(CustomerNotFoundException.class, () -> listAccounts.execute(new ListCustomerAccountsCommand(999L)));

        verify(customers).existsById(999L);
        verify(accounts, never()).findByCustomerId(anyLong());
    }

    private Account createAccountWithHolder(String accountNumber, Customer customer, String role) {
        Account account = new Account(accountNumber);
        AccountHolder holder = new AccountHolder(account, customer, role);
        account.getAccountHolders().add(holder);
        return account;
    }
}
