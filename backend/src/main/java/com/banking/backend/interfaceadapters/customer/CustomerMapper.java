package com.banking.backend.interfaceadapters.customer;

import com.banking.backend.domain.model.Customer;

import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toDomain(CustomerRequest request) {
        Customer customer = new Customer(
                request.documentNumber(),
                request.firstName(),
                request.lastName(),
                request.email()
        );
        customer.setPhone(request.phone());
        return customer;
    }

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getDocumentNumber(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getStatus(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
