package com.banking.backend.application.withdraw;

import com.banking.backend.domain.exception.AccountInactiveException;
import com.banking.backend.domain.exception.AccountNotFoundException;
import com.banking.backend.domain.exception.InsufficientFundsException;
import com.banking.backend.domain.enums.AccountStatus;
import com.banking.backend.domain.enums.TransactionStatus;
import com.banking.backend.domain.enums.TransactionType;
import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.model.Transaction;
import com.banking.backend.domain.repository.AccountRepository;
import com.banking.backend.domain.repository.TransactionRepository;
import com.banking.backend.interfaceadapters.withdraw.WithdrawResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class WithdrawFunds {

    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public WithdrawFunds(AccountRepository accounts, TransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @Transactional
    public WithdrawResponse execute(WithdrawFundsCommand command) {
        Account account = accounts.findByIdForUpdate(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        Optional<Transaction> existingOpt = transactions.findByIdempotencyKey(command.idempotencyKey());
        if (existingOpt.isPresent()) {
            Transaction existing = existingOpt.get();
            return WithdrawResponse.from(existing, existing.getSourceAccount().getBalance());
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountInactiveException(account.getId());
        }

        checkSufficientBalance(account, command.amount());

        Transaction transaction = createWithdrawalTransaction(command, account);
        updateAccountBalance(account, command.amount());

        transaction = transactions.save(transaction);
        return WithdrawResponse.from(transaction, account.getBalance());
    }

    private void checkSufficientBalance(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    account.getId(), amount, account.getBalance());
        }
    }

    private Transaction createWithdrawalTransaction(WithdrawFundsCommand command, Account account) {
        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                TransactionType.WITHDRAWAL,
                command.amount(),
                TransactionStatus.COMPLETED
        );
        transaction.setIdempotencyKey(command.idempotencyKey());
        transaction.setSourceAccount(account);
        return transaction;
    }

    private void updateAccountBalance(Account account, BigDecimal amount) {
        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        accounts.save(account);
    }
}
