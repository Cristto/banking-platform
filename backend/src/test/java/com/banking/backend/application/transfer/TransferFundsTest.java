package com.banking.backend.application.transfer;

import com.banking.backend.domain.enums.AccountStatus;
import com.banking.backend.domain.enums.TransactionStatus;
import com.banking.backend.domain.enums.TransactionType;
import com.banking.backend.domain.exception.AccountInactiveException;
import com.banking.backend.domain.exception.AccountNotFoundException;
import com.banking.backend.domain.exception.CurrencyMismatchException;
import com.banking.backend.domain.exception.InsufficientFundsException;
import com.banking.backend.domain.exception.SameAccountException;
import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.model.Customer;
import com.banking.backend.domain.model.Transaction;
import com.banking.backend.domain.repository.AccountRepository;
import com.banking.backend.domain.repository.TransactionRepository;
import com.banking.backend.interfaceadapters.transfer.TransferResponse;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransferFundsTest {

    private AccountRepository accounts;
    private TransactionRepository transactions;
    private TransferFunds transferFunds;

    @BeforeEach
    void setUp() {
        accounts = mock(AccountRepository.class);
        transactions = mock(TransactionRepository.class);
        transferFunds = new TransferFunds(accounts, transactions);
    }

    @Test
    void transfersSuccessfullyBetweenTwoActiveAccounts() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account source = createAccountWithId("ES111111111111111111111111111111", customer, 1L);
        source.setBalance(new BigDecimal("500.00"));
        Account destination = createAccountWithId("ES222222222222222222222222222222", customer, 2L);
        destination.setBalance(new BigDecimal("200.00"));

        when(accounts.findById(1L)).thenReturn(Optional.of(source));
        when(accounts.findById(2L)).thenReturn(Optional.of(destination));

        Transaction debit = createTransaction("tx-001", source, destination, new BigDecimal("100.00"));
        when(transactions.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());
        when(transactions.save(any(Transaction.class))).thenReturn(debit);

        TransferFundsCommand command = new TransferFundsCommand(1L, 2L, "key-1", new BigDecimal("100.00"));
        TransferResponse result = transferFunds.execute(command);

        assertNotNull(result);
        assertEquals(debit.getId(), result.transactionId());
        assertEquals(new BigDecimal("100.00"), result.amount());
        assertEquals(new BigDecimal("400.00"), result.sourceBalance());
        assertEquals(new BigDecimal("300.00"), result.destinationBalance());
        assertEquals("EUR", result.currency());
        assertEquals(TransactionStatus.COMPLETED.name(), result.status());
        verify(transactions, times(2)).save(any(Transaction.class));
    }

    @Test
    void returnsExistingTransactionsWhenIdempotencyKeyAlreadyUsed() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account source = createAccountWithId("ES111111111111111111111111111111", customer, 1L);
        source.setBalance(new BigDecimal("400.00"));
        Account destination = createAccountWithId("ES222222222222222222222222222222", customer, 2L);
        destination.setBalance(new BigDecimal("300.00"));

        when(accounts.findById(1L)).thenReturn(Optional.of(source));
        when(accounts.findById(2L)).thenReturn(Optional.of(destination));

        Transaction existingDebit = createTransaction("tx-002", source, destination, new BigDecimal("50.00"));
        when(transactions.findByIdempotencyKey("key-2")).thenReturn(Optional.of(existingDebit));

        TransferFundsCommand command = new TransferFundsCommand(1L, 2L, "key-2", new BigDecimal("50.00"));
        TransferResponse result = transferFunds.execute(command);

        assertNotNull(result);
        assertEquals(existingDebit.getId(), result.transactionId());
        assertEquals(new BigDecimal("50.00"), result.amount());
        assertEquals(source.getBalance(), result.sourceBalance());
        assertEquals(destination.getBalance(), result.destinationBalance());
        verify(transactions, never()).save(any());
    }

    @Test
    void throwsWhenSourceAccountDoesNotExist() {
        when(accounts.findById(999L)).thenReturn(Optional.empty());

        TransferFundsCommand command = new TransferFundsCommand(999L, 2L, "key-3", new BigDecimal("100.00"));

        assertThrows(AccountNotFoundException.class, () -> transferFunds.execute(command));
        verify(accounts).findById(999L);
        verify(transactions, never()).save(any());
    }

    @Test
    void throwsWhenDestinationAccountDoesNotExist() {
        Account source = createAccount("ES111111111111111111111111111111", new Customer("AB123", "Ana", "Garcia", "ana@example.com"));
        when(accounts.findById(1L)).thenReturn(Optional.of(source));
        when(accounts.findById(999L)).thenReturn(Optional.empty());

        TransferFundsCommand command = new TransferFundsCommand(1L, 999L, "key-4", new BigDecimal("100.00"));

        assertThrows(AccountNotFoundException.class, () -> transferFunds.execute(command));
        verify(accounts).findById(1L);
        verify(accounts).findById(999L);
        verify(transactions, never()).save(any());
    }

    @Test
    void throwsWhenSourceAccountIsNotActive() {
        Account source = createAccountWithId("ES111111111111111111111111111111", new Customer("AB123", "Ana", "Garcia", "ana@example.com"), 1L);
        source.setStatus(AccountStatus.CLOSED);
        Account destination = createAccountWithId("ES222222222222222222222222222222", new Customer("AB123", "Ana", "Garcia", "ana@example.com"), 2L);
        when(accounts.findById(1L)).thenReturn(Optional.of(source));
        when(accounts.findById(2L)).thenReturn(Optional.of(destination));

        TransferFundsCommand command = new TransferFundsCommand(1L, 2L, "key-5", new BigDecimal("100.00"));

        assertThrows(AccountInactiveException.class, () -> transferFunds.execute(command));
        verify(transactions, never()).save(any());
    }

    @Test
    void throwsWhenDestinationAccountIsNotActive() {
        Account source = createAccountWithId("ES111111111111111111111111111111", new Customer("AB123", "Ana", "Garcia", "ana@example.com"), 1L);
        Account destination = createAccountWithId("ES222222222222222222222222222222", new Customer("AB123", "Ana", "Garcia", "ana@example.com"), 2L);
        destination.setStatus(AccountStatus.CLOSED);
        when(accounts.findById(1L)).thenReturn(Optional.of(source));
        when(accounts.findById(2L)).thenReturn(Optional.of(destination));

        TransferFundsCommand command = new TransferFundsCommand(1L, 2L, "key-6", new BigDecimal("100.00"));

        assertThrows(AccountInactiveException.class, () -> transferFunds.execute(command));
        verify(transactions, never()).save(any());
    }

    @Test
    void throwsWhenTransferringToSameAccount() {
        Account source = createAccountWithId("ES111111111111111111111111111111", new Customer("AB123", "Ana", "Garcia", "ana@example.com"), 1L);
        when(accounts.findById(1L)).thenReturn(Optional.of(source));

        TransferFundsCommand command = new TransferFundsCommand(1L, 1L, "key-7", new BigDecimal("100.00"));

        assertThrows(SameAccountException.class, () -> transferFunds.execute(command));
        verify(transactions, never()).save(any());
    }

    @Test
    void throwsWhenAccountsHaveDifferentCurrencies() {
        Account source = createAccountWithId("ES111111111111111111111111111111", new Customer("AB123", "Ana", "Garcia", "ana@example.com"), 1L);
        source.setCurrency("USD");
        Account destination = createAccountWithId("ES222222222222222222222222222222", new Customer("AB123", "Ana", "Garcia", "ana@example.com"), 2L);
        destination.setCurrency("EUR");
        when(accounts.findById(1L)).thenReturn(Optional.of(source));
        when(accounts.findById(2L)).thenReturn(Optional.of(destination));

        TransferFundsCommand command = new TransferFundsCommand(1L, 2L, "key-8", new BigDecimal("100.00"));

        assertThrows(CurrencyMismatchException.class, () -> transferFunds.execute(command));
        verify(transactions, never()).save(any());
    }

    @Test
    void throwsWhenSourceHasInsufficientFunds() {
        Account source = createAccountWithId("ES111111111111111111111111111111", new Customer("AB123", "Ana", "Garcia", "ana@example.com"), 1L);
        source.setBalance(new BigDecimal("30.00"));
        Account destination = createAccountWithId("ES222222222222222222222222222222", new Customer("AB123", "Ana", "Garcia", "ana@example.com"), 2L);
        when(accounts.findById(1L)).thenReturn(Optional.of(source));
        when(accounts.findById(2L)).thenReturn(Optional.of(destination));

        TransferFundsCommand command = new TransferFundsCommand(1L, 2L, "key-9", new BigDecimal("100.00"));

        InsufficientFundsException ex = assertThrows(InsufficientFundsException.class, () -> transferFunds.execute(command));
        assertTrue(ex.getMessage().contains("insufficient funds"));
        assertTrue(ex.getMessage().contains("requested 100"));
        assertTrue(ex.getMessage().contains("available 30"));
        verify(transactions, never()).save(any());
    }

    @Test
    void rejectsZeroAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> new TransferFundsCommand(1L, 2L, "key-1", BigDecimal.ZERO));
    }

    @Test
    void rejectsNegativeAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> new TransferFundsCommand(1L, 2L, "key-2", new BigDecimal("-10.00")));
    }

    @Test
    void rejectsNullAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> new TransferFundsCommand(1L, 2L, "key-3", null));
    }

    @Test
    void rejectsBlankIdempotencyKey() {
        assertThrows(IllegalArgumentException.class,
                () -> new TransferFundsCommand(1L, 2L, "   ", new BigDecimal("100.00")));
    }

    @Test
    void rejectsNullSourceAccountId() {
        assertThrows(IllegalArgumentException.class,
                () -> new TransferFundsCommand(null, 2L, "key-4", new BigDecimal("100.00")));
    }

    @Test
    void rejectsNullDestinationAccountId() {
        assertThrows(IllegalArgumentException.class,
                () -> new TransferFundsCommand(1L, null, "key-5", new BigDecimal("100.00")));
    }

    @Test
    void returnsCorrectTransferResponseFields() {
        Customer customer = new Customer("CD456", "Pedro", "Lopez", "pedro@example.com");
        Account source = createAccountWithId("ES333333333333333333333333333333", customer, 3L);
        source.setBalance(new BigDecimal("500.00"));
        Account destination = createAccountWithId("ES444444444444444444444444444444", customer, 4L);
        destination.setBalance(new BigDecimal("200.00"));

        when(accounts.findById(3L)).thenReturn(Optional.of(source));
        when(accounts.findById(4L)).thenReturn(Optional.of(destination));

        Transaction debit = createTransaction("tx-003", source, destination, new BigDecimal("200.00"));
        debit.setIdempotencyKey("key-9");
        when(transactions.findByIdempotencyKey("key-9")).thenReturn(Optional.empty());
        when(transactions.save(any(Transaction.class))).thenReturn(debit);

        TransferFundsCommand command = new TransferFundsCommand(3L, 4L, "key-9", new BigDecimal("200.00"));
        TransferResponse result = transferFunds.execute(command);

        assertEquals(TransactionType.TRANSFER, debit.getType());
        assertEquals(TransactionStatus.COMPLETED, debit.getStatus());
        assertEquals("key-9", debit.getIdempotencyKey());
        assertNotNull(result.createdAt());
    }

    @Test
    void retriesOnOptimisticLockAndSucceedsOnSecondAttempt() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account source = createAccountWithId("ES111111111111111111111111111111", customer, 1L);
        source.setBalance(new BigDecimal("500.00"));
        Account destination = createAccountWithId("ES222222222222222222222222222222", customer, 2L);
        destination.setBalance(new BigDecimal("200.00"));

        when(accounts.findById(1L)).thenReturn(Optional.of(source));
        when(accounts.findById(2L)).thenReturn(Optional.of(destination));

        Transaction debit = createTransaction("tx-001", source, destination, new BigDecimal("100.00"));
        when(transactions.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());

        // First save fails with OptimisticLockException, second succeeds
        when(transactions.save(any(Transaction.class)))
                .thenThrow(new OptimisticLockException("Optimistic locking failed"))
                .thenReturn(debit)
                .thenReturn(debit);

        TransferFundsCommand command = new TransferFundsCommand(1L, 2L, "key-1", new BigDecimal("100.00"));
        TransferResponse result = transferFunds.execute(command);

        assertNotNull(result);
        assertEquals(debit.getId(), result.transactionId());
        assertEquals(new BigDecimal("300.00"), result.sourceBalance());
        assertEquals(new BigDecimal("400.00"), result.destinationBalance());
        verify(transactions, times(3)).save(any(Transaction.class)); // 1 failed debit + 1 debit + 1 credit
    }

    @Test
    void allowsExactBalanceTransfer() {
        Account source = createAccountWithId("ES111111111111111111111111111111", new Customer("AB123", "Ana", "Garcia", "ana@example.com"), 1L);
        source.setBalance(new BigDecimal("100.00"));
        Account destination = createAccountWithId("ES222222222222222222222222222222", new Customer("AB123", "Ana", "Garcia", "ana@example.com"), 2L);
        destination.setBalance(new BigDecimal("200.00"));

        when(accounts.findById(1L)).thenReturn(Optional.of(source));
        when(accounts.findById(2L)).thenReturn(Optional.of(destination));

        Transaction debit = createTransaction("tx-exact", source, destination, new BigDecimal("100.00"));
        when(transactions.findByIdempotencyKey("key-exact")).thenReturn(Optional.empty());
        when(transactions.save(any(Transaction.class))).thenReturn(debit);

        TransferFundsCommand command = new TransferFundsCommand(1L, 2L, "key-exact", new BigDecimal("100.00"));
        TransferResponse result = transferFunds.execute(command);

        assertNotNull(result);
        assertEquals(new BigDecimal("0.00"), result.sourceBalance());
        assertEquals(new BigDecimal("300.00"), result.destinationBalance());
    }

    @Test
    void rejectsTransferWhenSourceBalanceIsLessThanAmount() {
        Account source = createAccountWithId("ES111111111111111111111111111111", new Customer("AB123", "Ana", "Garcia", "ana@example.com"), 1L);
        source.setBalance(new BigDecimal("99.99"));
        Account destination = createAccountWithId("ES222222222222222222222222222222", new Customer("AB123", "Ana", "Garcia", "ana@example.com"), 2L);
        when(accounts.findById(1L)).thenReturn(Optional.of(source));
        when(accounts.findById(2L)).thenReturn(Optional.of(destination));

        TransferFundsCommand command = new TransferFundsCommand(1L, 2L, "key-insuff", new BigDecimal("100.00"));

        assertThrows(InsufficientFundsException.class, () -> transferFunds.execute(command));
        verify(transactions, never()).save(any());
    }

    private Account createAccount(String accountNumber, Customer customer) {
        Account account = new Account(accountNumber);
        account.setCustomerId(customer.getId());
        return account;
    }

    private Account createAccountWithId(String accountNumber, Customer customer, Long id) {
        Account account = createAccount(accountNumber, customer);
        account.setId(id);
        return account;
    }

    private Transaction createTransaction(String reference, Account source, Account destination, BigDecimal amount) {
        Transaction transaction = new Transaction(reference, TransactionType.TRANSFER, amount, TransactionStatus.COMPLETED);
        transaction.setSourceAccount(source);
        transaction.setDestinationAccount(destination);
        return transaction;
    }
}
