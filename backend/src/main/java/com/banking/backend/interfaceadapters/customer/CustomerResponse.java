package com.banking.backend.interfaceadapters.customer;

import com.banking.backend.domain.enums.CustomerStatus;

import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String documentNumber,
        String firstName,
        String lastName,
        String email,
        String phone,
        CustomerStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
