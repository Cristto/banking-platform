package com.banking.backend.interfaceadapters.customer;

import com.banking.backend.application.customer.RegisterCustomerUseCase;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final RegisterCustomerUseCase registerCustomerUseCase;

    public CustomerController(RegisterCustomerUseCase registerCustomerUseCase) {
        this.registerCustomerUseCase = registerCustomerUseCase;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> registerCustomer(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = registerCustomerUseCase.execute(request);
        return ResponseEntity.status(201).body(response);
    }
}
