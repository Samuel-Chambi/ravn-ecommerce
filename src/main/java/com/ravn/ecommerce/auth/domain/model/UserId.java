package com.ravn.ecommerce.auth.domain.model;

import java.util.Objects;
import java.util.UUID;

public class UserId {
    private final UUID value;
    private UserId(UUID value){
        this.value = Objects.requireNonNull(value);
    }

    public static UserId generate(){
        return new UserId(UUID.randomUUID());
    }

    public static UserId from(String value){
        return new UserId(UUID.fromString(value));
    }

    public UUID value(){
        return value;
    }

    @Override
    public boolean equals(Object object){
        if(this == object) return true;
        if(!(object instanceof UserId)) return false;
        UserId userId = (UserId) object;
        return value.equals(userId.value);
    }



}
