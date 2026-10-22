package com.banking.backend.application.account;

public record OpenAccountCommand(
        Long customerId,
        String role) {

    public OpenAccountCommand {
        customerId = required(customerId, "Customer ID");
        role = required(role, "Role", 20);
    }

    private static Long required(Long value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " must not be null");
        }
        return value;
    }

    private static String required(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(field + " must be at most " + maxLength + " characters");
        }
        return value.strip();
    }
}
