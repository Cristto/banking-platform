package com.banking.backend.application.account;

import com.banking.backend.domain.exception.CustomerNotFoundException;
import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.repository.AccountRepository;
import com.banking.backend.domain.repository.CustomerRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListCustomerAccounts {

    private final CustomerRepository customers;
    private final AccountRepository accounts;

    public ListCustomerAccounts(CustomerRepository customers, AccountRepository accounts) {
        this.customers = customers;
        this.accounts = accounts;
    }

    public List<Account> execute(ListCustomerAccountsCommand command) {
        if (!customers.existsById(command.customerId())) {
            throw new CustomerNotFoundException(command.customerId());
        }
        return accounts.findByCustomerId(command.customerId());
    }
}
