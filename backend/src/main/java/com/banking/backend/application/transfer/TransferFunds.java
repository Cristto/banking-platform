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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransferFunds {

    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public TransferFunds(AccountRepository accounts, TransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @Transactional
    public TransferResponse execute(TransferFundsCommand command) {
        validateSameAccount(command.sourceAccountId(), command.destinationAccountId());

        Long firstAccountId = Math.min(command.sourceAccountId(), command.destinationAccountId());
        Long secondAccountId = Math.max(command.sourceAccountId(), command.destinationAccountId());
        Account firstAccount = accounts.findByIdForUpdate(firstAccountId)
                .orElseThrow(() -> new AccountNotFoundException(firstAccountId));
        Account secondAccount = accounts.findByIdForUpdate(secondAccountId)
                .orElseThrow(() -> new AccountNotFoundException(secondAccountId));

        Account source = command.sourceAccountId().equals(firstAccountId) ? firstAccount : secondAccount;
        Account destination = command.destinationAccountId().equals(firstAccountId) ? firstAccount : secondAccount;

        Optional<Transaction> existingOpt = transactions.findByIdempotencyKey(command.idempotencyKey());
        if (existingOpt.isPresent()) {
            Transaction existing = existingOpt.get();
            if (isSameTransfer(existing, command)) {
                return TransferResponse.from(existing, existing.getSourceAccount().getBalance(),
                        existing.getDestinationAccount().getBalance());
            }
            throw new IllegalArgumentException("Idempotency key is already used for a different transfer");
        }

        validateSameCurrency(source, destination);
        validateSourceActive(source);
        validateDestinationActive(destination);
        validateSufficientFunds(source, command.amount());

        Transaction transfer = createTransferTransaction(command, source, destination);
        updateSourceBalance(source, command.amount());
        updateDestinationBalance(destination, command.amount());

        transfer = transactions.save(transfer);
        return TransferResponse.from(transfer, source.getBalance(), destination.getBalance());
    }

    private boolean isSameTransfer(Transaction transaction, TransferFundsCommand command) {
        return transaction.getType() == TransactionType.TRANSFER
                && transaction.getSourceAccount() != null
                && transaction.getDestinationAccount() != null
                && transaction.getSourceAccount().getId().equals(command.sourceAccountId())
                && transaction.getDestinationAccount().getId().equals(command.destinationAccountId())
                && transaction.getAmount().compareTo(command.amount()) == 0;
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

    private Transaction createTransferTransaction(TransferFundsCommand command, Account source, Account destination) {
        Transaction transaction = new Transaction(
                UUID.randomUUID().toString(),
                TransactionType.TRANSFER,
                command.amount(),
                TransactionStatus.COMPLETED
        );
        transaction.setIdempotencyKey(command.idempotencyKey());
        transaction.setSourceAccount(source);
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
