package com.ravn.ecommerce.auth.application.command;

public record RegisterUserCommand (
        String email,
        String password
){
}
