package com.ravn.ecommerce.presentation.rest.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String error;
    private String message;
    private String errorCode;
    private LocalDateTime timestamp;
    private String path;
    private Map<String, Object> details;
    private List<ValidationError> validationErrors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
        private Object rejectedValue;
    }

    public static ErrorResponse of(String error, String message, String errorCode) {
        return ErrorResponse.builder()
                .error(error)
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse of(String error, String message, String errorCode, String path) {
        return ErrorResponse.builder()
                .error(error)
                .message(message)
                .errorCode(errorCode)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
