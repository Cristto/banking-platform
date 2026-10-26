package com.banking.backend.application.deposit;

import com.banking.backend.domain.enums.AccountStatus;
import com.banking.backend.domain.enums.TransactionStatus;
import com.banking.backend.domain.enums.TransactionType;
import com.banking.backend.domain.exception.AccountInactiveException;
import com.banking.backend.domain.exception.AccountNotFoundException;
import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.model.Customer;
import com.banking.backend.domain.model.Transaction;
import com.banking.backend.domain.repository.AccountRepository;
import com.banking.backend.domain.repository.TransactionRepository;
import com.banking.backend.interfaceadapters.deposit.DepositResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DepositFundsTest {

    private AccountRepository accounts;
    private TransactionRepository transactions;
    private DepositFunds depositFunds;

    @BeforeEach
    void setUp() {
        accounts = mock(AccountRepository.class);
        transactions = mock(TransactionRepository.class);
        depositFunds = new DepositFunds(accounts, transactions);
    }

    @Test
    void depositsSuccessfullyWhenAccountExistsAndIsActive() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account account = createAccount("ES123456789012345678901234567890", customer);
        account.setBalance(new BigDecimal("100.00"));
        when(accounts.findById(1L)).thenReturn(Optional.of(account));

        Transaction savedTransaction = createTransaction("tx-001", account, new BigDecimal("50.00"));
        when(transactions.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());
        when(transactions.save(any(Transaction.class))).thenReturn(savedTransaction);

        DepositFundsCommand command = new DepositFundsCommand(1L, "key-1", new BigDecimal("50.00"));
        DepositResponse result = depositFunds.execute(command);

        assertNotNull(result);
        assertEquals(savedTransaction.getId(), result.transactionId());
        assertEquals(new BigDecimal("50.00"), result.amount());
        assertEquals(new BigDecimal("150.00"), result.balance());
        assertEquals("EUR", result.currency());
        assertEquals(TransactionStatus.COMPLETED.name(), result.status());
        verify(transactions).findByIdempotencyKey("key-1");
        verify(transactions).save(any(Transaction.class));
        assertEquals(new BigDecimal("150.00"), account.getBalance());
    }

    @Test
    void returnsExistingTransactionWhenIdempotencyKeyAlreadyUsed() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account account = createAccount("ES123456789012345678901234567890", customer);
        account.setBalance(new BigDecimal("200.00"));
        when(accounts.findById(1L)).thenReturn(Optional.of(account));

        Transaction existingTransaction = createTransaction("tx-002", account, new BigDecimal("25.00"));
        when(transactions.findByIdempotencyKey("key-2")).thenReturn(Optional.of(existingTransaction));

        DepositFundsCommand command = new DepositFundsCommand(1L, "key-2", new BigDecimal("25.00"));
        DepositResponse result = depositFunds.execute(command);

        assertNotNull(result);
        assertEquals(existingTransaction.getId(), result.transactionId());
        assertEquals(new BigDecimal("25.00"), result.amount());
        assertEquals(account.getBalance(), result.balance());
        verify(transactions, never()).save(any());
    }

    @Test
    void throwsWhenAccountDoesNotExist() {
        when(accounts.findById(999L)).thenReturn(Optional.empty());

        DepositFundsCommand command = new DepositFundsCommand(999L, "key-3", new BigDecimal("100.00"));

        assertThrows(AccountNotFoundException.class, () -> depositFunds.execute(command));
        verify(accounts).findById(999L);
        verify(transactions, never()).save(any());
    }

    @Test
    void throwsWhenAccountIsNotActive() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account account = createAccount("ES123456789012345678901234567890", customer);
        account.setStatus(AccountStatus.CLOSED);
        when(accounts.findById(2L)).thenReturn(Optional.of(account));

        DepositFundsCommand command = new DepositFundsCommand(2L, "key-4", new BigDecimal("100.00"));

        assertThrows(AccountInactiveException.class, () -> depositFunds.execute(command));
        verify(accounts).findById(2L);
        verify(transactions, never()).save(any());
    }

    @Test
    void rejectsZeroAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> new DepositFundsCommand(1L, "key-5", BigDecimal.ZERO));
    }

    @Test
    void rejectsNegativeAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> new DepositFundsCommand(1L, "key-6", new BigDecimal("-10.00")));
    }

    @Test
    void rejectsNullAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> new DepositFundsCommand(1L, "key-7", null));
    }

    @Test
    void rejectsBlankIdempotencyKey() {
        assertThrows(IllegalArgumentException.class,
                () -> new DepositFundsCommand(1L, "   ", new BigDecimal("100.00")));
    }

    @Test
    void rejectsNullAccountId() {
        assertThrows(IllegalArgumentException.class,
                () -> new DepositFundsCommand(null, "key-8", new BigDecimal("100.00")));
    }

    @Test
    void returnsCorrectDepositResponseFields() {
        Customer customer = new Customer("CD456", "Pedro", "Lopez", "pedro@example.com");
        Account account = createAccount("ES987654321098765432109876543210", customer);
        account.setBalance(new BigDecimal("500.00"));
        when(accounts.findById(3L)).thenReturn(Optional.of(account));

        Transaction savedTransaction = createTransaction("tx-003", account, new BigDecimal("100.00"));
        savedTransaction.setIdempotencyKey("key-9");
        when(transactions.findByIdempotencyKey("key-9")).thenReturn(Optional.empty());
        when(transactions.save(any(Transaction.class))).thenReturn(savedTransaction);

        DepositFundsCommand command = new DepositFundsCommand(3L, "key-9", new BigDecimal("100.00"));
        DepositResponse result = depositFunds.execute(command);

        assertEquals(TransactionType.DEPOSIT, savedTransaction.getType());
        assertEquals(TransactionStatus.COMPLETED, savedTransaction.getStatus());
        assertEquals("key-9", savedTransaction.getIdempotencyKey());
        assertEquals("ES987654321098765432109876543210", savedTransaction.getDestinationAccount().getAccountNumber());
        assertNotNull(result.createdAt());
    }

    private Account createAccount(String accountNumber, Customer customer) {
        Account account = new Account(accountNumber);
        account.setCustomerId(customer.getId());
        return account;
    }

    private Transaction createTransaction(String reference, Account account, BigDecimal amount) {
        Transaction transaction = new Transaction(reference, TransactionType.DEPOSIT, amount, TransactionStatus.COMPLETED);
        transaction.setDestinationAccount(account);
        return transaction;
    }
}
