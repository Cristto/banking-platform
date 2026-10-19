package com.banking.backend.interfaceadapters.customer;

public record RegisterCustomerRequest(
        String documentNumber, String firstName, String lastName, String email, String phone) {
}
