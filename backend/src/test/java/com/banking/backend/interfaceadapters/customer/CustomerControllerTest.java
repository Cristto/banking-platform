package com.banking.backend.interfaceadapters.customer;

import com.banking.backend.application.customer.RegisterCustomerUseCase;
import com.banking.backend.domain.enums.CustomerStatus;
import com.banking.backend.domain.exception.CustomerAlreadyExistsException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterCustomerUseCase registerCustomerUseCase;

    @Test
    void shouldRegisterCustomerWith201() throws Exception {
        // Arrange
        String requestBody = """
                {
                    "documentNumber": "12345678A",
                    "firstName": "Juan",
                    "lastName": "Pérez",
                    "email": "juan@example.com",
                    "phone": "600123456"
                }
                """;
        LocalDateTime now = LocalDateTime.now();
        CustomerResponse response = new CustomerResponse(
                1L, "12345678A", "Juan", "Pérez", "juan@example.com", "600123456",
                CustomerStatus.ACTIVE, now, now
        );

        when(registerCustomerUseCase.execute(any(CustomerRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.documentNumber").value("12345678A"))
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andExpect(jsonPath("$.lastName").value("Pérez"))
                .andExpect(jsonPath("$.email").value("juan@example.com"))
                .andExpect(jsonPath("$.phone").value("600123456"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(registerCustomerUseCase).execute(any(CustomerRequest.class));
        verifyNoMoreInteractions(registerCustomerUseCase);
    }

    @Test
    void shouldReturn409WhenCustomerAlreadyExists() throws Exception {
        // Arrange
        String requestBody = """
                {
                    "documentNumber": "12345678A",
                    "firstName": "Juan",
                    "lastName": "Pérez",
                    "email": "juan@example.com",
                    "phone": "600123456"
                }
                """;

        when(registerCustomerUseCase.execute(any(CustomerRequest.class)))
                .thenThrow(new CustomerAlreadyExistsException("12345678A"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").exists());

        verify(registerCustomerUseCase).execute(any(CustomerRequest.class));
    }

    @Test
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        // Arrange — missing required fields
        String invalidRequest = """
                {
                    "documentNumber": "",
                    "firstName": "",
                    "lastName": "",
                    "email": "invalid-email"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());

        verify(registerCustomerUseCase, never()).execute(any());
    }

    @Test
    void shouldReturn400WhenDocumentNumberExceedsLength() throws Exception {
        // Arrange
        String requestBody = """
                {
                    "documentNumber": "123456789012345678901234567890X",
                    "firstName": "Juan",
                    "lastName": "Pérez",
                    "email": "juan@example.com",
                    "phone": "600123456"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(registerCustomerUseCase, never()).execute(any());
    }
}
