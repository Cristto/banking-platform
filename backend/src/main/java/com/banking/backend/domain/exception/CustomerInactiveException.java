package com.banking.backend.domain.exception;

public class CustomerInactiveException extends RuntimeException {

    public CustomerInactiveException(Long customerId) {
        super("Customer with id " + customerId + " is not active");
    }
}
