package com.banking.backend.application.account;

import com.banking.backend.domain.exception.CustomerNotFoundException;
import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.repository.AccountRepository;

import org.springframework.stereotype.Service;

@Service
public class GetAccount {

    private final AccountRepository accounts;

    public GetAccount(AccountRepository accounts) {
        this.accounts = accounts;
    }

    public Account execute(GetAccountCommand command) {
        return accounts.findById(command.accountId())
                .orElseThrow(() -> new CustomerNotFoundException(command.accountId()));
    }
}
