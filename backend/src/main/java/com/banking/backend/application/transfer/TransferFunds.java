package com.banking.backend.application.transfer;

import com.banking.backend.domain.exception.AccountInactiveException;
import com.banking.backend.domain.exception.AccountNotFoundException;
import com.banking.backend.domain.exception.CurrencyMismatchException;
import com.banking.backend.domain.exception.InsufficientFundsException;
import com.banking.backend.domain.exception.SameAccountException;
import com.banking.backend.domain.enums.AccountStatus;
import com.banking.backend.domain.enums.TransactionStatus;
import com.banking.backend.domain.enums.TransactionType;
import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.model.Transaction;
import com.banking.backend.domain.repository.AccountRepository;
import com.banking.backend.domain.repository.TransactionRepository;
import com.banking.backend.interfaceadapters.transfer.TransferResponse;
import jakarta.persistence.OptimisticLockException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransferFunds {

    private static final int MAX_RETRIES = 3;

    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public TransferFunds(AccountRepository accounts, TransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @Transactional
    public TransferResponse execute(TransferFundsCommand command) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            Account source = accounts.findById(command.sourceAccountId())
                    .orElseThrow(() -> new AccountNotFoundException(command.sourceAccountId()));

            Account destination = accounts.findById(command.destinationAccountId())
                    .orElseThrow(() -> new AccountNotFoundException(command.destinationAccountId()));

            validateSameAccount(source.getId(), destination.getId());
            validateSameCurrency(source, destination);
            validateSourceActive(source);
            validateDestinationActive(destination);
            validateSufficientFunds(source, command.amount());

            // Check idempotency at the start of each retry
            Optional<Transaction> existingOpt = transactions.findByIdempotencyKey(command.idempotencyKey());
            if (existingOpt.isPresent()) {
                Transaction existing = existingOpt.get();
                return TransferResponse.from(existing, existing.getSourceAccount().getBalance(),
                        existing.getDestinationAccount().getBalance());
            }

            String transferId = UUID.randomUUID().toString();

            Transaction debit = createDebitTransaction(transferId, command, source);
            Transaction credit = createCreditTransaction(transferId, command, destination);

            updateSourceBalance(source, command.amount());
            updateDestinationBalance(destination, command.amount());

            try {
                transactions.save(debit);
                transactions.save(credit);
                return TransferResponse.from(debit, source.getBalance(), destination.getBalance());
            } catch (OptimisticLockException e) {
                if (attempt == MAX_RETRIES - 1) {
                    throw e;
                }
            }
        }
        throw new IllegalStateException("Unreachable");
    }

    private void validateSameAccount(Long sourceId, Long destinationId) {
        if (sourceId.equals(destinationId)) {
            throw new SameAccountException(sourceId);
        }
    }

    private void validateSameCurrency(Account source, Account destination) {
        if (!source.getCurrency().equals(destination.getCurrency())) {
            throw new CurrencyMismatchException(source.getId(), destination.getId(),
                    source.getCurrency(), destination.getCurrency());
        }
    }

    private void validateSourceActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountInactiveException(account.getId());
        }
    }

    private void validateDestinationActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountInactiveException(account.getId());
        }
    }

    private void validateSufficientFunds(Account source, BigDecimal amount) {
        if (source.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(source.getId(), amount, source.getBalance());
        }
    }

    private Transaction createDebitTransaction(String transferId, TransferFundsCommand command, Account source) {
        Transaction transaction = new Transaction(
                transferId,
                TransactionType.TRANSFER,
                command.amount(),
                TransactionStatus.COMPLETED
        );
        transaction.setIdempotencyKey(command.idempotencyKey());
        transaction.setSourceAccount(source);
        return transaction;
    }

    private Transaction createCreditTransaction(String transferId, TransferFundsCommand command, Account destination) {
        Transaction transaction = new Transaction(
                transferId,
                TransactionType.TRANSFER,
                command.amount(),
                TransactionStatus.COMPLETED
        );
        transaction.setIdempotencyKey(command.idempotencyKey());
        transaction.setDestinationAccount(destination);
        return transaction;
    }

    private void updateSourceBalance(Account account, BigDecimal amount) {
        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        accounts.save(account);
    }

    private void updateDestinationBalance(Account account, BigDecimal amount) {
        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);
        accounts.save(account);
    }
}
