package com.banking.backend.infrastructure.persistence.repository;

import com.banking.backend.domain.model.Customer;
import com.banking.backend.domain.repository.CustomerRepository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerJpaRepository extends JpaRepository<Customer, Long>, CustomerRepository {

    default Optional<Customer> findByDocumentNumber(String documentNumber) {
        return findByDocumentNumberIgnoreCase(documentNumber);
    }

    Optional<Customer> findByDocumentNumberIgnoreCase(String documentNumber);

    List<Customer> findAllByOrderByLastNameAscFirstNameAscIdAsc();

    List<Customer> findByDocumentNumberContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrderByLastNameAscFirstNameAscIdAsc(
            String documentNumber, String firstName, String lastName);

    @Override
    default List<Customer> search(String query) {
        return findByDocumentNumberContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrderByLastNameAscFirstNameAscIdAsc(
                query, query, query);
    }
}
