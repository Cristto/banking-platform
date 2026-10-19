package com.banking.backend.domain.repository;

import com.banking.backend.domain.model.Customer;

public interface CustomerRepository {
    boolean existsByDocumentNumber(String documentNumber);
    boolean existsByEmail(String email);
    Customer save(Customer customer);
}
