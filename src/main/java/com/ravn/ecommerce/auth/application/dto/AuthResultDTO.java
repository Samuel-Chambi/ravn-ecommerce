package com.ravn.ecommerce.auth.application.dto;

import com.ravn.ecommerce.auth.domain.model.User;

public record AuthResultDTO(
        String userId ,
        String role,
        String authToken
){
    public static AuthResultDTO from(User user, String token){
        return new AuthResultDTO(
                user.getUserId().toString(),
                user.getUserRole().name(),
                token
        );
    }
}
