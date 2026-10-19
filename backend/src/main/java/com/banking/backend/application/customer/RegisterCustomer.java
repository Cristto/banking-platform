package com.banking.backend.application.customer;

import com.banking.backend.domain.exception.DuplicateCustomerException;
import com.banking.backend.domain.model.Customer;
import com.banking.backend.domain.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterCustomer {
    private final CustomerRepository customers;

    public RegisterCustomer(CustomerRepository customers) {
        this.customers = customers;
    }

    @Transactional
    public Customer execute(RegisterCustomerCommand command) {
        if (customers.existsByDocumentNumber(command.documentNumber())) {
            throw new DuplicateCustomerException("A customer with this document number already exists");
        }
        if (customers.existsByEmail(command.email())) {
            throw new DuplicateCustomerException("A customer with this email already exists");
        }
        Customer customer = new Customer(command.documentNumber(), command.firstName(),
                command.lastName(), command.email());
        customer.setPhone(command.phone());
        return customers.save(customer);
    }
}
