package com.banking.backend.infrastructure.persistence.customer;

import com.banking.backend.domain.exception.DuplicateCustomerException;
import com.banking.backend.domain.model.Customer;
import com.banking.backend.domain.repository.CustomerRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class JpaCustomerRepository implements CustomerRepository {
    private static final Set<String> UNIQUE_CUSTOMER_CONSTRAINTS = Set.of(
            "customers_document_number_key",
            "customers_email_key",
            "customers_document_number_normalized_key",
            "customers_email_normalized_key");

    private final SpringDataCustomerRepository repository;

    public JpaCustomerRepository(SpringDataCustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByDocumentNumber(String documentNumber) {
        return repository.existsByDocumentNumberIgnoreCase(documentNumber);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmailIgnoreCase(email);
    }

    @Override
    public Customer save(Customer customer) {
        return repository.save(customer);
    }

    @Override
    public List<Customer> findAll() {
        return repository.findAllByOrderByLastNameAscFirstNameAscIdAsc();
    }

    @Override
    public List<Customer> search(String query) {
        return repository
                .findByDocumentNumberContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrderByLastNameAscFirstNameAscIdAsc(
                        query, query, query);
    }

    @Override
    public java.util.Optional<Customer> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }
}
