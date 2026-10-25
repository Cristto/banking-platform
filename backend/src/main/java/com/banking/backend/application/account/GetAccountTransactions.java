package com.banking.backend.application.account;

import com.banking.backend.domain.exception.AccountNotFoundException;
import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.repository.AccountRepository;
import com.banking.backend.domain.repository.TransactionRepository;
import com.banking.backend.interfaceadapters.account.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetAccountTransactions {

    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public GetAccountTransactions(AccountRepository accounts, TransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> execute(GetAccountTransactionsCommand command) {
        Account account = accounts.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

        Page<TransactionResponse> transactionsPage = transactions
                .findBySourceAccountIdOrDestinationAccountId(
                        command.accountId(),
                        command.accountId(),
                        command.pageable()
                )
                .map(TransactionResponse::from);

        return transactionsPage;
    }
}
