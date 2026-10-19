package com.banking.backend.interfaceadapters.customer;

import com.banking.backend.domain.enums.CustomerStatus;
import com.banking.backend.domain.model.Customer;
import java.time.LocalDateTime;

public record CustomerResponse(Long id, String documentNumber, String firstName, String lastName,
        String email, String phone, CustomerStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getDocumentNumber(), customer.getFirstName(),
                customer.getLastName(), customer.getEmail(), customer.getPhone(), customer.getStatus(),
                customer.getCreatedAt(), customer.getUpdatedAt());
    }
}
