package com.banking.backend.application.customer;

import com.banking.backend.domain.enums.CustomerStatus;
import com.banking.backend.domain.exception.CustomerAlreadyExistsException;
import com.banking.backend.domain.model.Customer;
import com.banking.backend.domain.repository.CustomerRepository;
import com.banking.backend.interfaceadapters.customer.CustomerMapper;
import com.banking.backend.interfaceadapters.customer.CustomerRequest;
import com.banking.backend.interfaceadapters.customer.CustomerResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterCustomerUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private RegisterCustomerUseCase useCase;

    @Test
    void shouldRegisterCustomerSuccessfully() {
        // Arrange
        CustomerRequest request = new CustomerRequest("12345678A", "Juan", "Pérez", "juan@example.com", "600123456");
        LocalDateTime now = LocalDateTime.now();

        Customer domainCustomer = mock(Customer.class);

        CustomerResponse expectedResponse = new CustomerResponse(
                1L, "12345678A", "Juan", "Pérez", "juan@example.com", "600123456",
                CustomerStatus.ACTIVE, now, now
        );

        when(customerRepository.findByDocumentNumber("12345678A")).thenReturn(Optional.empty());
        when(customerMapper.toDomain(request)).thenReturn(domainCustomer);
        when(customerRepository.save(domainCustomer)).thenReturn(domainCustomer);
        when(customerMapper.toResponse(domainCustomer)).thenReturn(expectedResponse);

        // Act
        CustomerResponse result = useCase.execute(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("12345678A", result.documentNumber());
        assertEquals("juan@example.com", result.email());

        verify(customerRepository).findByDocumentNumber("12345678A");
        verify(customerMapper).toDomain(request);
        verify(customerRepository).save(domainCustomer);
        verify(customerMapper).toResponse(domainCustomer);
    }

    @Test
    void shouldThrowWhenCustomerAlreadyExists() {
        // Arrange
        CustomerRequest request = new CustomerRequest("12345678A", "Juan", "Pérez", "juan@example.com", "600123456");
        Customer existingCustomer = mock(Customer.class);

        when(customerRepository.findByDocumentNumber("12345678A")).thenReturn(Optional.of(existingCustomer));

        // Act & Assert
        CustomerAlreadyExistsException exception = assertThrows(
                CustomerAlreadyExistsException.class,
                () -> useCase.execute(request)
        );
        assertEquals("Customer with document number 12345678A already exists", exception.getMessage());

        verify(customerRepository).findByDocumentNumber("12345678A");
        verify(customerMapper, never()).toDomain(any());
        verify(customerRepository, never()).save(any());
    }
}
