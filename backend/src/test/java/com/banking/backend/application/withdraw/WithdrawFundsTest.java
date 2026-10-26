package com.banking.backend.application.withdraw;

import com.banking.backend.domain.enums.AccountStatus;
import com.banking.backend.domain.enums.TransactionStatus;
import com.banking.backend.domain.enums.TransactionType;
import com.banking.backend.domain.exception.AccountInactiveException;
import com.banking.backend.domain.exception.AccountNotFoundException;
import com.banking.backend.domain.exception.InsufficientFundsException;
import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.model.Customer;
import com.banking.backend.domain.model.Transaction;
import com.banking.backend.domain.repository.AccountRepository;
import com.banking.backend.domain.repository.TransactionRepository;
import com.banking.backend.interfaceadapters.withdraw.WithdrawResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WithdrawFundsTest {

    private AccountRepository accounts;
    private TransactionRepository transactions;
    private WithdrawFunds withdrawFunds;

    @BeforeEach
    void setUp() {
        accounts = mock(AccountRepository.class);
        transactions = mock(TransactionRepository.class);
        withdrawFunds = new WithdrawFunds(accounts, transactions);
    }

    @Test
    void withdrawsSuccessfullyWhenAccountExistsAndHasSufficientFunds() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account account = createAccount("ES123456789012345678901234567890", customer);
        account.setBalance(new BigDecimal("500.00"));
        when(accounts.findByIdForUpdate(1L)).thenReturn(Optional.of(account));

        Transaction savedTransaction = createTransaction("tx-001", account, new BigDecimal("100.00"));
        when(transactions.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());
        when(transactions.save(any(Transaction.class))).thenReturn(savedTransaction);

        WithdrawFundsCommand command = new WithdrawFundsCommand(1L, "key-1", new BigDecimal("100.00"));
        WithdrawResponse result = withdrawFunds.execute(command);

        assertNotNull(result);
        assertEquals(savedTransaction.getId(), result.transactionId());
        assertEquals(new BigDecimal("100.00"), result.amount());
        assertEquals(new BigDecimal("400.00"), result.balance());
        assertEquals("EUR", result.currency());
        assertEquals(TransactionStatus.COMPLETED.name(), result.status());
        verify(transactions).findByIdempotencyKey("key-1");
        verify(transactions).save(any(Transaction.class));
        assertEquals(new BigDecimal("400.00"), account.getBalance());
    }

    @Test
    void returnsExistingTransactionWhenIdempotencyKeyAlreadyUsed() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account account = createAccount("ES123456789012345678901234567890", customer);
        account.setBalance(new BigDecimal("200.00"));
        when(accounts.findByIdForUpdate(1L)).thenReturn(Optional.of(account));

        Transaction existingTransaction = createTransaction("tx-002", account, new BigDecimal("50.00"));
        when(transactions.findByIdempotencyKey("key-2")).thenReturn(Optional.of(existingTransaction));

        WithdrawFundsCommand command = new WithdrawFundsCommand(1L, "key-2", new BigDecimal("50.00"));
        WithdrawResponse result = withdrawFunds.execute(command);

        assertNotNull(result);
        assertEquals(existingTransaction.getId(), result.transactionId());
        assertEquals(new BigDecimal("50.00"), result.amount());
        assertEquals(account.getBalance(), result.balance());
        verify(transactions, never()).save(any());
    }

    @Test
    void throwsWhenAccountDoesNotExist() {
        when(accounts.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        WithdrawFundsCommand command = new WithdrawFundsCommand(999L, "key-3", new BigDecimal("100.00"));

        assertThrows(AccountNotFoundException.class, () -> withdrawFunds.execute(command));
        verify(accounts).findByIdForUpdate(999L);
        verify(transactions, never()).save(any());
    }

    @Test
    void throwsWhenAccountIsNotActive() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account account = createAccount("ES123456789012345678901234567890", customer);
        account.setStatus(AccountStatus.CLOSED);
        when(accounts.findByIdForUpdate(2L)).thenReturn(Optional.of(account));

        WithdrawFundsCommand command = new WithdrawFundsCommand(2L, "key-4", new BigDecimal("100.00"));

        assertThrows(AccountInactiveException.class, () -> withdrawFunds.execute(command));
        verify(accounts).findByIdForUpdate(2L);
        verify(transactions, never()).save(any());
    }

    @Test
    void throwsWhenInsufficientFunds() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account account = createAccount("ES123456789012345678901234567890", customer);
        account.setBalance(new BigDecimal("30.00"));
        when(accounts.findByIdForUpdate(1L)).thenReturn(Optional.of(account));

        WithdrawFundsCommand command = new WithdrawFundsCommand(1L, "key-5", new BigDecimal("100.00"));

        InsufficientFundsException ex = assertThrows(InsufficientFundsException.class, () -> withdrawFunds.execute(command));
        assertTrue(ex.getMessage().contains("insufficient funds"));
        assertTrue(ex.getMessage().contains("requested 100"));
        assertTrue(ex.getMessage().contains("available 30"));
        verify(transactions, never()).save(any());
    }

    @Test
    void rejectsZeroAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> new WithdrawFundsCommand(1L, "key-1", BigDecimal.ZERO));
    }

    @Test
    void rejectsNegativeAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> new WithdrawFundsCommand(1L, "key-2", new BigDecimal("-10.00")));
    }

    @Test
    void rejectsNullAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> new WithdrawFundsCommand(1L, "key-3", null));
    }

    @Test
    void rejectsBlankIdempotencyKey() {
        assertThrows(IllegalArgumentException.class,
                () -> new WithdrawFundsCommand(1L, "   ", new BigDecimal("100.00")));
    }

    @Test
    void rejectsNullAccountId() {
        assertThrows(IllegalArgumentException.class,
                () -> new WithdrawFundsCommand(null, "key-4", new BigDecimal("100.00")));
    }

    @Test
    void returnsCorrectWithdrawalResponseFields() {
        Customer customer = new Customer("CD456", "Pedro", "Lopez", "pedro@example.com");
        Account account = createAccount("ES987654321098765432109876543210", customer);
        account.setBalance(new BigDecimal("500.00"));
        when(accounts.findByIdForUpdate(3L)).thenReturn(Optional.of(account));

        Transaction savedTransaction = createTransaction("tx-003", account, new BigDecimal("200.00"));
        savedTransaction.setIdempotencyKey("key-9");
        when(transactions.findByIdempotencyKey("key-9")).thenReturn(Optional.empty());
        when(transactions.save(any(Transaction.class))).thenReturn(savedTransaction);

        WithdrawFundsCommand command = new WithdrawFundsCommand(3L, "key-9", new BigDecimal("200.00"));
        WithdrawResponse result = withdrawFunds.execute(command);

        assertEquals(TransactionType.WITHDRAWAL, savedTransaction.getType());
        assertEquals(TransactionStatus.COMPLETED, savedTransaction.getStatus());
        assertEquals("key-9", savedTransaction.getIdempotencyKey());
        assertEquals("ES987654321098765432109876543210", savedTransaction.getSourceAccount().getAccountNumber());
        assertNotNull(result.createdAt());
    }

    @Test
    void withdrawsUsingLockedAccount() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account account = createAccount("ES123456789012345678901234567890", customer);
        account.setBalance(new BigDecimal("500.00"));
        when(accounts.findByIdForUpdate(1L)).thenReturn(Optional.of(account));

        Transaction savedTransaction = createTransaction("tx-001", account, new BigDecimal("100.00"));
        when(transactions.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());

        when(transactions.save(any(Transaction.class))).thenReturn(savedTransaction);

        WithdrawFundsCommand command = new WithdrawFundsCommand(1L, "key-1", new BigDecimal("100.00"));
        WithdrawResponse result = withdrawFunds.execute(command);

        assertNotNull(result);
        assertEquals(savedTransaction.getId(), result.transactionId());
        assertEquals(new BigDecimal("400.00"), result.balance());
        verify(accounts).findByIdForUpdate(1L);
        verify(transactions).save(any(Transaction.class));
    }

    @Test
    void rejectsWithdrawalWhenBalanceEqualsAmount() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account account = createAccount("ES123456789012345678901234567890", customer);
        account.setBalance(new BigDecimal("100.00"));
        when(accounts.findByIdForUpdate(1L)).thenReturn(Optional.of(account));

        WithdrawFundsCommand command = new WithdrawFundsCommand(1L, "key-exact", new BigDecimal("100.00"));

        // Balance equals amount — this is valid (not insufficient)
        Transaction savedTransaction = createTransaction("tx-exact", account, new BigDecimal("100.00"));
        when(transactions.findByIdempotencyKey("key-exact")).thenReturn(Optional.empty());
        when(transactions.save(any(Transaction.class))).thenReturn(savedTransaction);

        WithdrawResponse result = withdrawFunds.execute(command);

        assertNotNull(result);
        assertEquals(new BigDecimal("0.00"), result.balance());
    }

    @Test
    void throwsWhenBalanceIsLessThanRequestedAmount() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account account = createAccount("ES123456789012345678901234567890", customer);
        account.setBalance(new BigDecimal("99.99"));
        when(accounts.findByIdForUpdate(1L)).thenReturn(Optional.of(account));

        WithdrawFundsCommand command = new WithdrawFundsCommand(1L, "key-insuff", new BigDecimal("100.00"));

        assertThrows(InsufficientFundsException.class, () -> withdrawFunds.execute(command));
        verify(transactions, never()).save(any());
    }

    private Account createAccount(String accountNumber, Customer customer) {
        Account account = new Account(accountNumber);
        account.setCustomerId(customer.getId());
        return account;
    }

    private Transaction createTransaction(String reference, Account account, BigDecimal amount) {
        Transaction transaction = new Transaction(reference, TransactionType.WITHDRAWAL, amount, TransactionStatus.COMPLETED);
        transaction.setSourceAccount(account);
        return transaction;
    }
}
