package com.ravn.ecommerce.infrastructure.persistence.jpa.mapper;

public interface Mapper <I , O>{
    O toDomain(I jpaEntity);
    I toJpaEntity(O domainEntity);
}
