package com.banking.backend.application.customer;

import com.banking.backend.domain.exception.CustomerAlreadyExistsException;
import com.banking.backend.domain.model.Customer;
import com.banking.backend.domain.repository.CustomerRepository;
import com.banking.backend.interfaceadapters.customer.CustomerRequest;
import com.banking.backend.interfaceadapters.customer.CustomerResponse;
import com.banking.backend.interfaceadapters.customer.CustomerMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public RegisterCustomerUseCase(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Transactional
    public CustomerResponse execute(CustomerRequest request) {
        // Check for duplicate document number
        customerRepository.findByDocumentNumber(request.documentNumber())
                .ifPresent(existing -> {
                    throw new CustomerAlreadyExistsException(request.documentNumber());
                });

        // Map request to domain model
        Customer customer = customerMapper.toDomain(request);

        // Persist and map to response
        Customer saved = customerRepository.save(customer);
        return customerMapper.toResponse(saved);
    }
}
