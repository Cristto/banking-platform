package com.banking.backend.application.customer;

import java.util.Locale;

public record RegisterCustomerCommand(
        String documentNumber, String firstName, String lastName, String email, String phone) {

    public RegisterCustomerCommand {
        documentNumber = required(documentNumber, "Document number", 30).toUpperCase(Locale.ROOT);
        firstName = required(firstName, "First name", 100);
        lastName = required(lastName, "Last name", 150);
        email = required(email, "Email", 255).toLowerCase(Locale.ROOT);
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new IllegalArgumentException("Email must be valid");
        }
        phone = phone == null || phone.isBlank() ? null : phone.strip();
        if (phone != null && (phone.length() > 30 || !phone.matches("\\+?[0-9() .-]{3,30}")
                || phone.chars().noneMatch(Character::isDigit))) {
            throw new IllegalArgumentException("Phone must be valid and contain at most 30 characters");
        }
    }

    private static String required(String value, String field, int maxLength) {
        if (value == null || value.isBlank() || value.strip().length() > maxLength) {
            throw new IllegalArgumentException(field + " is required and must contain at most " + maxLength + " characters");
        }
        return value.strip();
    }
}
