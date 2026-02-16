package com.ravn.ecommerce.domain.exceptions;

public class UnauthorizedException extends DomainException {
    public UnauthorizedException(String message){
        super(message , "UNAUTHORIZED");
    }
    public UnauthorizedException(){
        super("Unauthorized access" , "UNAUTHORIZED");
    }
}
