package com.banking.backend.application.account;

import com.banking.backend.domain.enums.AccountStatus;
import com.banking.backend.domain.enums.TransactionStatus;
import com.banking.backend.domain.enums.TransactionType;
import com.banking.backend.domain.exception.AccountNotFoundException;
import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.model.Customer;
import com.banking.backend.domain.model.Transaction;
import com.banking.backend.domain.repository.AccountRepository;
import com.banking.backend.domain.repository.TransactionRepository;
import com.banking.backend.interfaceadapters.account.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GetAccountTransactionsTest {

    private AccountRepository accounts;
    private TransactionRepository transactions;
    private GetAccountTransactions getAccountTransactions;

    @BeforeEach
    void setUp() {
        accounts = mock(AccountRepository.class);
        transactions = mock(TransactionRepository.class);
        getAccountTransactions = new GetAccountTransactions(accounts, transactions);
    }

    @Test
    void returnsTransactionHistoryWhenAccountExists() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account account = createAccount("ES123456789012345678901234567890", customer);
        account.setBalance(new BigDecimal("500.00"));
        when(accounts.findById(1L)).thenReturn(Optional.of(account));

        Transaction deposit = createDepositTransaction("tx-001", account, new BigDecimal("100.00"));
        deposit.setId(1L);
        Transaction withdrawal = createWithdrawalTransaction("tx-002", account, new BigDecimal("50.00"));
        withdrawal.setId(2L);
        List<Transaction> transactionsList = List.of(deposit, withdrawal);
        Page<Transaction> transactionPage = new PageImpl<>(transactionsList, PageRequest.of(0, 20), 2);
        when(transactions.findBySourceAccountIdOrDestinationAccountId(eq(1L), eq(1L), any(Pageable.class)))
                .thenReturn(transactionPage);

        Pageable pageable = PageRequest.of(0, 20);
        Page<TransactionResponse> result = getAccountTransactions.execute(new GetAccountTransactionsCommand(1L, pageable));

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).transactionId());
        assertEquals(2L, result.getContent().get(1).transactionId());
        verify(accounts).findById(1L);
        verify(transactions).findBySourceAccountIdOrDestinationAccountId(eq(1L), eq(1L), any(Pageable.class));
    }

    @Test
    void returnsEmptyPageWhenAccountHasNoTransactions() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account account = createAccount("ES123456789012345678901234567890", customer);
        when(accounts.findById(1L)).thenReturn(Optional.of(account));

        Page<Transaction> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(transactions.findBySourceAccountIdOrDestinationAccountId(eq(1L), eq(1L), any(Pageable.class)))
                .thenReturn(emptyPage);

        Pageable pageable = PageRequest.of(0, 20);
        Page<TransactionResponse> result = getAccountTransactions.execute(new GetAccountTransactionsCommand(1L, pageable));

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getContent().size());
        verify(accounts).findById(1L);
        verify(transactions).findBySourceAccountIdOrDestinationAccountId(eq(1L), eq(1L), any(Pageable.class));
    }

    @Test
    void throwsWhenAccountDoesNotExist() {
        when(accounts.findById(999L)).thenReturn(Optional.empty());

        Pageable pageable = PageRequest.of(0, 20);

        assertThrows(AccountNotFoundException.class,
                () -> getAccountTransactions.execute(new GetAccountTransactionsCommand(999L, pageable)));
        verify(accounts).findById(999L);
        verify(transactions, never()).findBySourceAccountIdOrDestinationAccountId(anyLong(), anyLong(), any());
    }

    @Test
    void rejectsNullAccountId() {
        assertThrows(IllegalArgumentException.class,
                () -> new GetAccountTransactionsCommand(null, PageRequest.of(0, 20)));
    }

    @Test
    void rejectsNullPageable() {
        assertThrows(IllegalArgumentException.class,
                () -> new GetAccountTransactionsCommand(1L, null));
    }

    @Test
    void returnsCorrectTransactionResponseFields() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account account = createAccount("ES123456789012345678901234567890", customer);
        account.setBalance(new BigDecimal("500.00"));
        when(accounts.findById(1L)).thenReturn(Optional.of(account));

        Transaction deposit = createDepositTransaction("tx-001", account, new BigDecimal("100.00"));
        deposit.setId(1L);
        List<Transaction> transactionsList = List.of(deposit);
        Page<Transaction> transactionPage = new PageImpl<>(transactionsList, PageRequest.of(0, 20), 1);
        when(transactions.findBySourceAccountIdOrDestinationAccountId(eq(1L), eq(1L), any(Pageable.class)))
                .thenReturn(transactionPage);

        Pageable pageable = PageRequest.of(0, 20);
        Page<TransactionResponse> result = getAccountTransactions.execute(new GetAccountTransactionsCommand(1L, pageable));

        TransactionResponse response = result.getContent().get(0);
        assertEquals(deposit.getId(), response.transactionId());
        assertEquals("tx-001", response.reference());
        assertEquals(TransactionType.DEPOSIT.name(), response.type());
        assertEquals(new BigDecimal("100.00"), response.amount());
        assertEquals(new BigDecimal("500.00"), response.balance());
        assertEquals("EUR", response.currency());
        assertEquals(TransactionStatus.COMPLETED.name(), response.status());
        assertEquals("ES123456789012345678901234567890", response.destinationAccountNumber());
        assertNull(response.sourceAccountNumber());
        assertNotNull(response.createdAt());
    }

    @Test
    void returnsWithdrawalTransactionWithSourceAccount() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account account = createAccount("ES123456789012345678901234567890", customer);
        account.setBalance(new BigDecimal("400.00"));
        when(accounts.findById(1L)).thenReturn(Optional.of(account));

        Transaction withdrawal = createWithdrawalTransaction("tx-003", account, new BigDecimal("100.00"));
        withdrawal.setId(3L);
        List<Transaction> transactionsList = List.of(withdrawal);
        Page<Transaction> transactionPage = new PageImpl<>(transactionsList, PageRequest.of(0, 20), 1);
        when(transactions.findBySourceAccountIdOrDestinationAccountId(eq(1L), eq(1L), any(Pageable.class)))
                .thenReturn(transactionPage);

        Pageable pageable = PageRequest.of(0, 20);
        Page<TransactionResponse> result = getAccountTransactions.execute(new GetAccountTransactionsCommand(1L, pageable));

        TransactionResponse response = result.getContent().get(0);
        assertEquals(TransactionType.WITHDRAWAL.name(), response.type());
        assertEquals("ES123456789012345678901234567890", response.sourceAccountNumber());
        assertNull(response.destinationAccountNumber());
        assertEquals(new BigDecimal("400.00"), response.balance());
    }

    @Test
    void returnsTransferTransactionWithBothAccounts() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account sourceAccount = createAccount("ES123456789012345678901234567890", customer);
        Account destAccount = createAccount("ES987654321098765432109876543210", customer);
        destAccount.setBalance(new BigDecimal("600.00"));

        when(accounts.findById(1L)).thenReturn(Optional.of(sourceAccount));

        Transaction transfer = createTransferTransaction("tx-004", sourceAccount, destAccount, new BigDecimal("100.00"));
        List<Transaction> transactionsList = List.of(transfer);
        Page<Transaction> transactionPage = new PageImpl<>(transactionsList, PageRequest.of(0, 20), 1);
        when(transactions.findBySourceAccountIdOrDestinationAccountId(eq(1L), eq(1L), any(Pageable.class)))
                .thenReturn(transactionPage);

        Pageable pageable = PageRequest.of(0, 20);
        Page<TransactionResponse> result = getAccountTransactions.execute(new GetAccountTransactionsCommand(1L, pageable));

        TransactionResponse response = result.getContent().get(0);
        assertEquals(TransactionType.TRANSFER.name(), response.type());
        assertEquals("ES123456789012345678901234567890", response.sourceAccountNumber());
        assertEquals("ES987654321098765432109876543210", response.destinationAccountNumber());
        assertEquals(new BigDecimal("600.00"), response.balance());
        assertEquals("EUR", response.currency());
    }

    @Test
    void supportsPagination() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account account = createAccount("ES123456789012345678901234567890", customer);
        when(accounts.findById(1L)).thenReturn(Optional.of(account));

        Transaction tx1 = createDepositTransaction("tx-001", account, new BigDecimal("10.00"));
        tx1.setId(1L);
        Transaction tx2 = createDepositTransaction("tx-002", account, new BigDecimal("20.00"));
        tx2.setId(2L);
        Transaction tx3 = createDepositTransaction("tx-003", account, new BigDecimal("30.00"));
        tx3.setId(3L);
        List<Transaction> transactionsList = List.of(tx1, tx2, tx3);
        Page<Transaction> transactionPage = new PageImpl<>(transactionsList, PageRequest.of(0, 2), 3);
        when(transactions.findBySourceAccountIdOrDestinationAccountId(eq(1L), eq(1L), any(Pageable.class)))
                .thenReturn(transactionPage);

        Pageable pageable = PageRequest.of(0, 2);
        Page<TransactionResponse> result = getAccountTransactions.execute(new GetAccountTransactionsCommand(1L, pageable));

        // Spring Data's PageImpl returns all content when not sliced; verify pagination metadata
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void returnsTransactionsFromDestinationAccountPerspective() {
        Customer customer = new Customer("AB123", "Ana", "Garcia", "ana@example.com");
        Account sourceAccount = createAccount("ES123456789012345678901234567890", customer);
        Account destAccount = createAccount("ES987654321098765432109876543210", customer);
        destAccount.setBalance(new BigDecimal("600.00"));

        // Querying the destination account should show the transfer as incoming
        when(accounts.findById(2L)).thenReturn(Optional.of(destAccount));

        Transaction transfer = createTransferTransaction("tx-005", sourceAccount, destAccount, new BigDecimal("100.00"));
        List<Transaction> transactionsList = List.of(transfer);
        Page<Transaction> transactionPage = new PageImpl<>(transactionsList, PageRequest.of(0, 20), 1);
        when(transactions.findBySourceAccountIdOrDestinationAccountId(eq(2L), eq(2L), any(Pageable.class)))
                .thenReturn(transactionPage);

        Pageable pageable = PageRequest.of(0, 20);
        Page<TransactionResponse> result = getAccountTransactions.execute(new GetAccountTransactionsCommand(2L, pageable));

        TransactionResponse response = result.getContent().get(0);
        assertEquals("ES987654321098765432109876543210", response.destinationAccountNumber());
        assertEquals("ES123456789012345678901234567890", response.sourceAccountNumber());
    }

    private Account createAccount(String accountNumber, Customer customer) {
        Account account = new Account(accountNumber);
        account.setCustomerId(customer.getId());
        return account;
    }

    private Transaction createDepositTransaction(String reference, Account account, BigDecimal amount) {
        Transaction transaction = new Transaction(reference, TransactionType.DEPOSIT, amount, TransactionStatus.COMPLETED);
        transaction.setDestinationAccount(account);
        return transaction;
    }

    private Transaction createWithdrawalTransaction(String reference, Account account, BigDecimal amount) {
        Transaction transaction = new Transaction(reference, TransactionType.WITHDRAWAL, amount, TransactionStatus.COMPLETED);
        transaction.setSourceAccount(account);
        return transaction;
    }

    private Transaction createTransferTransaction(String reference, Account sourceAccount, Account destAccount, BigDecimal amount) {
        Transaction transaction = new Transaction(reference, TransactionType.TRANSFER, amount, TransactionStatus.COMPLETED);
        transaction.setSourceAccount(sourceAccount);
        transaction.setDestinationAccount(destAccount);
        return transaction;
    }
}
