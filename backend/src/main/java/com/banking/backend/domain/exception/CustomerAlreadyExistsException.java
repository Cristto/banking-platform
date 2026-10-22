package com.banking.backend.domain.exception;

public class CustomerAlreadyExistsException extends RuntimeException {

    public CustomerAlreadyExistsException(String documentNumber) {
        super("Customer with document number " + documentNumber + " already exists");
    }
}
