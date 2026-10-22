package com.banking.backend.application.account;

import com.banking.backend.domain.exception.CustomerNotFoundException;
import com.banking.backend.domain.model.Account;
import com.banking.backend.domain.model.Customer;
import com.banking.backend.domain.repository.AccountRepository;
import com.banking.backend.domain.repository.CustomerRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OpenAccount {

    private final CustomerRepository customers;
    private final AccountRepository accounts;

    public OpenAccount(CustomerRepository customers, AccountRepository accounts) {
        this.customers = customers;
        this.accounts = accounts;
    }

    @Transactional
    public Account execute(OpenAccountCommand command) {
        Customer customer = customers.findById(command.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(command.customerId()));

        if (customer.getStatus() != com.banking.backend.domain.enums.CustomerStatus.ACTIVE) {
            throw new IllegalArgumentException("Only active customers can open accounts");
        }

        Account account = Account.openForCustomer(customer, command.role());
        return accounts.saveWithHolder(account, customer, command.role());
    }
}
