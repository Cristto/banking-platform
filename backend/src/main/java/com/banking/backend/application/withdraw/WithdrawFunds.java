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
import jakarta.persistence.OptimisticLockException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class WithdrawFunds {

    private static final int MAX_RETRIES = 3;

    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public WithdrawFunds(AccountRepository accounts, TransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @Transactional
    public WithdrawResponse execute(WithdrawFundsCommand command) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            Account account = accounts.findById(command.accountId())
                    .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

            if (account.getStatus() != AccountStatus.ACTIVE) {
                throw new AccountInactiveException(account.getId());
            }

            Optional<Transaction> existingOpt = transactions.findByIdempotencyKey(command.idempotencyKey());
            if (existingOpt.isPresent()) {
                Transaction existing = existingOpt.get();
                return WithdrawResponse.from(existing, existing.getSourceAccount().getBalance());
            }

            checkSufficientBalance(account, command.amount());

            Transaction transaction = createWithdrawalTransaction(command, account);
            updateAccountBalance(account, command.amount());

            try {
                transaction = transactions.save(transaction);
                return WithdrawResponse.from(transaction, account.getBalance());
            } catch (OptimisticLockException e) {
                // Another thread modified the account; retry with fresh state
                if (attempt == MAX_RETRIES - 1) {
                    throw e;
                }
            }
        }
        throw new IllegalStateException("Unreachable");
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
