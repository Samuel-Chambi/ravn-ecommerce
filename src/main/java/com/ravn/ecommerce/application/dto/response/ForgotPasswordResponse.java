package com.ravn.ecommerce.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ForgotPasswordResponse {
    private String message;
    private String resetToken;
}
