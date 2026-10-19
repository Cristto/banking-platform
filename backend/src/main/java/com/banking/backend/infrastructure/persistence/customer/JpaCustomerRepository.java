package com.banking.backend.infrastructure.persistence.customer;

import com.banking.backend.domain.exception.DuplicateCustomerException;
import com.banking.backend.domain.model.Customer;
import com.banking.backend.domain.repository.CustomerRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
public class JpaCustomerRepository implements CustomerRepository {
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
        try {
            // Flush here so concurrent duplicate registrations are translated inside the adapter.
            return repository.saveAndFlush(customer);
        } catch (DataIntegrityViolationException exception) {
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof ConstraintViolationException violation
                        && "23505".equals(violation.getSQLState())) {
                    throw new DuplicateCustomerException("A customer with this document number or email already exists");
                }
            }
            throw exception;
        }
    }
}
