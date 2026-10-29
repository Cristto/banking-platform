package com.banking.backend.application.customer;

import com.banking.backend.domain.model.Customer;
import com.banking.backend.domain.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SearchCustomersTest {
    private CustomerRepository customers;
    private SearchCustomers searchCustomers;

    @BeforeEach
    void setUp() {
        customers = mock(CustomerRepository.class);
        searchCustomers = new SearchCustomers(customers);
    }

    @Test
    void returnsAllCustomersWhenQueryIsNull() {
        List<Customer> allCustomers = List.of(
                new Customer("111", "Ana", "Garcia", "ana@example.com"),
                new Customer("222", "Luis", "Martinez", "luis@example.com")
        );
        when(customers.findAll()).thenReturn(allCustomers);

        List<Customer> result = searchCustomers.execute(null);

        assertEquals(allCustomers, result);
        verify(customers).findAll();
        verify(customers, never()).search(any());
    }

    @Test
    void returnsAllCustomersWhenQueryIsBlank() {
        List<Customer> allCustomers = List.of(
                new Customer("111", "Ana", "Garcia", "ana@example.com")
        );
        when(customers.findAll()).thenReturn(allCustomers);

        List<Customer> result = searchCustomers.execute("   ");

        assertEquals(allCustomers, result);
        verify(customers).findAll();
        verify(customers, never()).search(any());
    }

    @Test
    void searchesByQueryWhenProvided() {
        List<Customer> searchResults = List.of(
                new Customer("12345678A", "Juan", "Pérez", "juan@example.com")
        );
        when(customers.search("12345678A")).thenReturn(searchResults);

        List<Customer> result = searchCustomers.execute("12345678A");

        assertEquals(searchResults, result);
        verify(customers).search("12345678A");
        verify(customers, never()).findAll();
    }

    @Test
    void stripsQueryBeforeSearching() {
        List<Customer> searchResults = List.of(
                new Customer("12345678A", "Juan", "Pérez", "juan@example.com")
        );
        when(customers.search("12345678A")).thenReturn(searchResults);

        searchCustomers.execute("  12345678A  ");

        verify(customers).search("12345678A");
        verify(customers, never()).findAll();
    }

    @Test
    void returnsListWithMatchingCustomerWhenQueryIsProvided() {
        List<Customer> searchResults = List.of(
                new Customer("111", "Ana", "Garcia", "ana@example.com")
        );
        when(customers.search("111")).thenReturn(searchResults);

        List<Customer> result = searchCustomers.execute("111");

        assertEquals(1, result.size());
        assertEquals("Ana", result.get(0).getFirstName());
    }
}
