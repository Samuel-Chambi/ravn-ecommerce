package com.ravn.ecommerce.presentation.rest.exceptions;

import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/test");
    }

    @Test
    void shouldHandleEntityNotFound() {
        EntityNotFoundException ex = new EntityNotFoundException("Product", 123L);

        ResponseEntity<ErrorResponse> response = handler.handleEntityNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("ENTITY_NOT_FOUND");
        assertThat(response.getBody().getMessage()).contains("Product");
    }
}