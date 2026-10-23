package com.banking.backend.application.account;

public record OpenAccountCommand(
        Long customerId) {

    public OpenAccountCommand {
        customerId = required(customerId, "Customer ID");
    }

    private static Long required(Long value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " must not be null");
        }
        return value;
    }
}
