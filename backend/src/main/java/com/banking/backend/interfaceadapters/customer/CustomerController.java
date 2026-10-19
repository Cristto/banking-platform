package com.banking.backend.interfaceadapters.customer;

import com.banking.backend.application.customer.RegisterCustomer;
import com.banking.backend.application.customer.RegisterCustomerCommand;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final RegisterCustomer registerCustomer;

    public CustomerController(RegisterCustomer registerCustomer) {
        this.registerCustomer = registerCustomer;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse register(@RequestBody RegisterCustomerRequest request) {
        return CustomerResponse.from(registerCustomer.execute(new RegisterCustomerCommand(
                request.documentNumber(), request.firstName(), request.lastName(), request.email(), request.phone())));
    }
}
