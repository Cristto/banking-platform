package com.banking.backend.application.account;

import com.banking.backend.domain.exception.AccountNotFoundException;
import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.model.Customer;
import com.banking.backend.domain.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetAccountTest {

    private AccountRepository accounts;
    private GetAccount getAccount;

    @BeforeEach
    void setUp() {
        accounts = mock(AccountRepository.class);
        getAccount = new GetAccount(accounts);
    }

    @Test
    void returnsAccountDetailsWhenAccountExists() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account account = createAccount("ES123456789012345678901234567890", customer);
        account.setBalance(new BigDecimal("1500.50"));
        when(accounts.findById(1L)).thenReturn(Optional.of(account));

        Account result = getAccount.execute(new GetAccountCommand(1L));

        assertNotNull(result);
        assertEquals("ES123456789012345678901234567890", result.getAccountNumber());
        assertEquals(new BigDecimal("1500.50"), result.getBalance());
        assertEquals("EUR", result.getCurrency());
        assertEquals(customer.getId(), result.getCustomerId());
        verify(accounts).findById(1L);
    }

    @Test
    void throwsWhenAccountDoesNotExist() {
        when(accounts.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> getAccount.execute(new GetAccountCommand(999L)));

        verify(accounts).findById(999L);
    }

    @Test
    void returnsAccountWithZeroBalanceWhenNoOperationsPerformed() {
        Customer customer = new Customer("CD456", "Pedro", "Lopez", "pedro@example.com");
        Account account = createAccount("ES987654321098765432109876543210", customer);
        when(accounts.findById(2L)).thenReturn(Optional.of(account));

        Account result = getAccount.execute(new GetAccountCommand(2L));

        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertEquals("EUR", result.getCurrency());
    }

    private Account createAccount(String accountNumber, Customer customer) {
        Account account = new Account(accountNumber);
        account.setCustomerId(customer.getId());
        return account;
    }
}
