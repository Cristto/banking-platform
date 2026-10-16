package com.banking.backend.domain.repository;

import com.banking.backend.domain.model.Customer;

import java.util.Optional;

public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findByDocumentNumber(String documentNumber);

    Optional<Customer> findById(Long id);
}
