package com.banking.backend.application.account;

public record ListCustomerAccountsCommand(
        Long customerId) {

    public ListCustomerAccountsCommand {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID must not be null");
        }
    }
}
