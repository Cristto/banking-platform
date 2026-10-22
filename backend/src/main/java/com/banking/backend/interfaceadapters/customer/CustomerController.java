package com.banking.backend.interfaceadapters.customer;

import com.banking.backend.application.customer.RegisterCustomer;
import com.banking.backend.application.customer.RegisterCustomerCommand;
import com.banking.backend.application.customer.SearchCustomers;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final RegisterCustomer registerCustomer;
    private final SearchCustomers searchCustomers;

    public CustomerController(RegisterCustomer registerCustomer, SearchCustomers searchCustomers) {
        this.registerCustomer = registerCustomer;
        this.searchCustomers = searchCustomers;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse register(@RequestBody RegisterCustomerRequest request) {
        return CustomerResponse.from(registerCustomer.execute(new RegisterCustomerCommand(
                request.documentNumber(), request.firstName(), request.lastName(), request.email(), request.phone())));
    }

    @GetMapping
    public List<CustomerResponse> search(@RequestParam(required = false) String query) {
        return searchCustomers.execute(query).stream()
                .map(CustomerResponse::from)
                .toList();
    }
}
