package com.banking.backend.application.account;

import com.banking.backend.domain.exception.AccountInactiveException;
import com.banking.backend.domain.exception.AccountNotFoundException;
import com.banking.backend.domain.enums.AccountStatus;
import com.banking.backend.domain.enums.TransactionStatus;
import com.banking.backend.domain.enums.TransactionType;
import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.model.Transaction;
import com.banking.backend.domain.repository.AccountRepository;
import com.banking.backend.domain.repository.TransactionRepository;
import com.banking.backend.interfaceadapters.account.DepositResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class DepositFunds {

    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public DepositFunds(AccountRepository accounts, TransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @Transactional
    public DepositResponse execute(DepositFundsCommand command) {
        Account account = accounts.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountInactiveException(account.getId());
        }

        Optional<Transaction> existingOpt = transactions.findByIdempotencyKey(command.idempotencyKey());
        if (existingOpt.isPresent()) {
            Transaction existing = existingOpt.get();
            return DepositResponse.from(existing, existing.getDestinationAccount().getBalance());
        }

        Transaction transaction = createDepositTransaction(command, account);
        updateAccountBalance(account, command.amount());

        transaction = transactions.save(transaction);
        return DepositResponse.from(transaction, account.getBalance());
    }

    private Transaction createDepositTransaction(DepositFundsCommand command, Account account) {
        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                TransactionType.DEPOSIT,
                command.amount(),
                TransactionStatus.COMPLETED
        );
        transaction.setIdempotencyKey(command.idempotencyKey());
        transaction.setDestinationAccount(account);
        return transaction;
    }

    private void updateAccountBalance(Account account, BigDecimal amount) {
        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);
        accounts.save(account);
    }
}
