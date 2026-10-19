package com.banking.backend.infrastructure.persistence.customer;

import com.banking.backend.domain.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByDocumentNumberIgnoreCase(String documentNumber);
    boolean existsByEmailIgnoreCase(String email);
}
