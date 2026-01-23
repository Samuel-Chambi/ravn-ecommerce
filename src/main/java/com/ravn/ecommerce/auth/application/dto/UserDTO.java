package com.ravn.ecommerce.auth.application.dto;

import com.ravn.ecommerce.auth.domain.model.User;

public record UserDTO(
        String id,
        String email,
        String role
){
    public static UserDTO from(User user){
        return new UserDTO(
                user.getUserId().toString(),
                user.getEmail().toString(),
                user.getUserRole().name()
        );
    }
}
