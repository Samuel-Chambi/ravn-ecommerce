package com.ravn.ecommerce.presentation.rest.exceptions;

import com.ravn.ecommerce.domain.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
        // DOMAIN EXCEPTIONS
        @ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleEntityNotFound(
                        EntityNotFoundException ex,
                        HttpServletRequest request) {
                log.warn("Entity not found: {}", ex.getMessage());
                ErrorResponse errorResponse = ErrorResponse.of(
                                "Not Found",
                                ex.getMessage(),
                                ex.getErrorCode(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        @ExceptionHandler(BusinessRuleViolation.class)
        public ResponseEntity<ErrorResponse> handleBusinessRuleViolation(
                        BusinessRuleViolation ex,
                        HttpServletRequest request) {
                log.warn("Business rule violation: {}", ex.getMessage());
                ErrorResponse errorResponse = ErrorResponse.of(
                                "Bad request",
                                ex.getMessage(),
                                ex.getErrorCode(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ErrorResponse> handleDuplicateResource(
                        DuplicateResourceException ex,
                        HttpServletRequest request) {
                log.warn("Duplicate resource: {}", ex.getMessage());
                ErrorResponse errorResponse = ErrorResponse.of(
                                "Conflict",
                                ex.getMessage(),
                                "DUPLICATE_RESOURCE",
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }

        @ExceptionHandler(InsufficientStockException.class)
        public ResponseEntity<ErrorResponse> handleInsufficientStock(
                        InsufficientStockException ex,
                        HttpServletRequest request) {
                log.warn("Insufficient stock: {}", ex.getMessage());
                ErrorResponse errorResponse = ErrorResponse.of(
                                "Bad Request",
                                ex.getMessage(),
                                ex.getErrorCode(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        @ExceptionHandler(InvalidOperationException.class)
        public ResponseEntity<ErrorResponse> handleInvalidOperation(
                        InvalidOperationException ex,
                        HttpServletRequest request) {
                log.warn("Invalid operation: {}", ex.getMessage());
                ErrorResponse errorResponse = ErrorResponse.of(
                                "Bad Request",
                                ex.getMessage(),
                                ex.getErrorCode(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        @ExceptionHandler(DomainException.class)
        public ResponseEntity<ErrorResponse> handleDomainException(
                        DomainException ex,
                        HttpServletRequest request) {
                log.warn("Domain exception: {}", ex.getMessage());
                ErrorResponse errorResponse = ErrorResponse.of(
                                "Bad Request",
                                ex.getMessage(),
                                ex.getErrorCode(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // VALIDATION EXCEPTIONS
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationErrors(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                log.warn("Validation error: {}", ex.getMessage());
                List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> ErrorResponse.ValidationError.builder()
                                                .field(error.getField())
                                                .message(error.getDefaultMessage())
                                                .rejectedValue(error.getRejectedValue())
                                                .build())
                                .toList();
                ErrorResponse errorResponse = ErrorResponse.builder()
                                .error("Bad Request")
                                .message("Validation failed")
                                .errorCode("VALIDATION_ERROR")
                                .path(request.getRequestURI())
                                .timestamp(LocalDateTime.now())
                                .validationErrors(validationErrors)
                                .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ErrorResponse> handleConstraintViolation(
                        ConstraintViolationException ex,
                        HttpServletRequest request) {
                log.warn("Constraint violation: {}", ex.getMessage());
                List<ErrorResponse.ValidationError> validationErrors = ex.getConstraintViolations()
                                .stream()
                                .map(violation -> ErrorResponse.ValidationError.builder()
                                                .field(violation.getPropertyPath().toString())
                                                .message(violation.getMessage())
                                                .rejectedValue(violation.getInvalidValue())
                                                .build())
                                .toList();
                ErrorResponse errorResponse = ErrorResponse.builder()
                                .error("Bad Request")
                                .message("Constraint violation")
                                .errorCode("CONSTRAINT_VIOLATION")
                                .path(request.getRequestURI())
                                .timestamp(LocalDateTime.now())
                                .validationErrors(validationErrors)
                                .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        @ExceptionHandler(HandlerMethodValidationException.class)
        public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(
                        HandlerMethodValidationException ex,
                        HttpServletRequest request) {
                log.warn("Handler method validation failed: {}", ex.getMessage());

                // Extract validation error messages
                String errorMessage = ex.getAllErrors().stream()
                                .map(MessageSourceResolvable::getDefaultMessage)
                                .filter(msg -> msg != null && !msg.isEmpty())
                                .findFirst()
                                .orElse("Invalid image file(s)");

                ErrorResponse errorResponse = ErrorResponse.of(
                                "Bad Request",
                                errorMessage,
                                "VALIDATION_ERROR",
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleTypeMismatch(
                        MethodArgumentTypeMismatchException ex,
                        HttpServletRequest request) {

                log.warn("Type mismatch: {}", ex.getMessage());

                String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                                ex.getValue(),
                                ex.getName(),
                                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

                ErrorResponse errorResponse = ErrorResponse.of(
                                "Bad Request",
                                message,
                                "TYPE_MISMATCH",
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // SECURITY EXCEPTIONS
        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ErrorResponse> handleUnauthorized(
                        UnauthorizedException ex,
                        HttpServletRequest request) {
                log.warn("Unauthorized: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.of(
                                "Unauthorized",
                                ex.getMessage(),
                                ex.getErrorCode(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ErrorResponse> handleAuthentication(
                        AuthenticationException ex,
                        HttpServletRequest request) {
                log.warn("Authentication failed: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.of(
                                "Unauthorized",
                                "Authentication failed",
                                "AUTHENTICATION_FAILED",
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentials(
                        BadCredentialsException ex,
                        HttpServletRequest request) {
                log.warn("Bad credentials: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.of(
                                "Unauthorized",
                                "Invalid email or password",
                                "INVALID_CREDENTIALS",
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDenied(
                        AccessDeniedException ex,
                        HttpServletRequest request) {
                log.warn("Access denied: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.of(
                                "Forbidden",
                                "You don't have permission to access this resource",
                                "ACCESS_DENIED",
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        // GENERIC EXCEPTIONS
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgument(
                        IllegalArgumentException ex,
                        HttpServletRequest request) {
                log.warn("Illegal argument: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.of(
                                "Bad Request",
                                ex.getMessage(),
                                "ILLEGAL_ARGUMENT",
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        @ExceptionHandler(TooManyRequestsException.class)
        public ResponseEntity<ErrorResponse> handleTooManyRequestsException(
                TooManyRequestsException ex,
                HttpServletRequest request) {
                log.warn("Rate limit exceeded: {}", ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.of(
                        "Too many requests",
                        ex.getMessage(),
                        ex.getErrorCode(),
                        request.getRequestURI());

                return ResponseEntity.status(ex.getStatus()).body(errorResponse);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(
                        Exception ex,
                        HttpServletRequest request) {

                log.error("Unexpected error occurred", ex);

                ErrorResponse errorResponse = ErrorResponse.of(
                                "Internal Server Error",
                                "An unexpected error occurred. Please try again later.",
                                "INTERNAL_SERVER_ERROR",
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
}
