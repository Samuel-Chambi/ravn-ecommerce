package com.ravn.ecommerce.auth.domain.model;

import com.ravn.ecommerce.auth.domain.exception.UserInactiveException;
import com.ravn.ecommerce.auth.domain.service.PasswordPolicy;

import java.time.Instant;

public class User {
    private final UserId userId;
    private final Email email;
    private PasswordHash passwordHash;
    private final UserRole userRole;
    private UserStatus userStatus;
    private final Instant createdAt;


    public User(UserId userId , Email email, PasswordHash passwordHash, UserRole userRole, UserStatus userStatus, Instant createdAt) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.userRole = userRole;
        this.userStatus = userStatus;
        this.createdAt = createdAt;
        this.userId = userId;
    }

    public void changePassword (PasswordHash newPasswordHash, PasswordPolicy passwordPolicy){
        passwordPolicy.validate(newPasswordHash);
        this.passwordHash = newPasswordHash;
    }

    public void ensureActiveUser(){
        if(this.userStatus != UserStatus.ACTIVE){
            throw new UserInactiveException();
        }
    }

    /*Add more getters according to logic application requirements*/
    public PasswordHash getPassword(){
        return passwordHash;
    }
    public UserId getUserId(){
        return userId;
    }
    public UserRole getUserRole(){
        return userRole;
    }
    public Email getEmail(){
        return email;
    }
    public boolean isActive(){
        return this.userStatus == UserStatus.ACTIVE;
    }
}
