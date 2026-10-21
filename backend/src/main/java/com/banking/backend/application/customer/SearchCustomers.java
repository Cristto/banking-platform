package com.banking.backend.application.customer;

import com.banking.backend.domain.model.Customer;
import com.banking.backend.domain.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SearchCustomers {
    private final CustomerRepository customers;

    public SearchCustomers(CustomerRepository customers) {
        this.customers = customers;
    }

    @Transactional(readOnly = true)
    public List<Customer> execute(String query) {
        if (query == null || query.isBlank()) {
            return customers.findAll();
        }
        return customers.search(query.strip());
    }
}
