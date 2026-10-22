package com.banking.backend.domain.repository;

import com.banking.backend.domain.model.Customer;

import java.util.List;

public interface CustomerRepository {
    boolean existsByDocumentNumber(String documentNumber);
    boolean existsByEmail(String email);
    Customer save(Customer customer);
    List<Customer> findAll();
    List<Customer> search(String query);
    java.util.Optional<Customer> findById(Long id);
}
