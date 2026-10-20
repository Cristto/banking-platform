package com.banking.backend.application.customer;

import com.banking.backend.domain.exception.DuplicateCustomerException;
import com.banking.backend.domain.model.Customer;
import com.banking.backend.domain.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegisterCustomerTest {
    private CustomerRepository customers;
    private RegisterCustomer registerCustomer;

    @BeforeEach
    void setUp() {
        customers = mock(CustomerRepository.class);
        registerCustomer = new RegisterCustomer(customers);
    }

    @Test
    void registersCustomerWhenDocumentNumberAndEmailAreAvailable() {
        RegisterCustomerCommand command = new RegisterCustomerCommand(
                "ab123", "Ada", "Lovelace", "ada@example.com", "+34 600 123 456");
        when(customers.existsByDocumentNumber(command.documentNumber())).thenReturn(false);
        when(customers.existsByEmail(command.email())).thenReturn(false);
        when(customers.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = registerCustomer.execute(command);

        assertEquals(command.documentNumber(), result.getDocumentNumber());
        assertEquals(command.firstName(), result.getFirstName());
        assertEquals(command.lastName(), result.getLastName());
        assertEquals(command.email(), result.getEmail());
        assertEquals(command.phone(), result.getPhone());
        verify(customers).existsByDocumentNumber(command.documentNumber());
        verify(customers).existsByEmail(command.email());
        verify(customers).save(any(Customer.class));
    }

    @Test
    void throwsWhenDocumentNumberAlreadyExists() {
        RegisterCustomerCommand command = new RegisterCustomerCommand(
                "AB123", "Ada", "Lovelace", "ada@example.com", null);
        when(customers.existsByDocumentNumber(command.documentNumber())).thenReturn(true);

        assertThrows(DuplicateCustomerException.class, () -> registerCustomer.execute(command));

        verify(customers).existsByDocumentNumber(command.documentNumber());
        verify(customers, never()).existsByEmail(any());
        verify(customers, never()).save(any());
    }

    @Test
    void throwsWhenEmailAlreadyExists() {
        RegisterCustomerCommand command = new RegisterCustomerCommand(
                "AB123", "Ada", "Lovelace", "ada@example.com", null);
        when(customers.existsByDocumentNumber(command.documentNumber())).thenReturn(false);
        when(customers.existsByEmail(command.email())).thenReturn(true);

        assertThrows(DuplicateCustomerException.class, () -> registerCustomer.execute(command));

        verify(customers).existsByDocumentNumber(command.documentNumber());
        verify(customers).existsByEmail(command.email());
        verify(customers, never()).save(any());
    }

    @Test
    void returnsCustomerSavedByRepository() {
        RegisterCustomerCommand command = new RegisterCustomerCommand(
                "AB123", "Ada", "Lovelace", "ada@example.com", null);
        Customer savedCustomer = new Customer(command.documentNumber(), command.firstName(),
                command.lastName(), command.email());
        when(customers.existsByDocumentNumber(command.documentNumber())).thenReturn(false);
        when(customers.existsByEmail(command.email())).thenReturn(false);
        when(customers.save(any(Customer.class))).thenReturn(savedCustomer);

        Customer result = registerCustomer.execute(command);

        assertSame(savedCustomer, result);
        assertNull(result.getPhone());
    }
}
