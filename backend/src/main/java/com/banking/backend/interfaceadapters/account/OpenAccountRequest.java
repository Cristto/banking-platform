package com.banking.backend.interfaceadapters.account;

public record OpenAccountRequest(
        Long customerId,
        String role) {
}
