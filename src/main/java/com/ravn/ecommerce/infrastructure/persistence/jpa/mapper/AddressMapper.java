package com.ravn.ecommerce.infrastructure.persistence.jpa.mapper;

import com.ravn.ecommerce.domain.model.user.Address;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.AddressJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper implements Mapper<AddressJpaEntity , Address>{
    @Override
    public Address toDomain(AddressJpaEntity jpaEntity) {
        return Address.builder()
                .id(jpaEntity.getId())
                .userId(jpaEntity.getUserId())
                .fullName(jpaEntity.getFullName())
                .phone(jpaEntity.getPhone())
                .street(jpaEntity.getStreet())
                .city(jpaEntity.getCity())
                .state(jpaEntity.getState())
                .country(jpaEntity.getCountry())
                .zipCode(jpaEntity.getZipCode())
                .isDefault(jpaEntity.isDefault())
                .createdAt(jpaEntity.getCreatedAt())
                .updatedAt(jpaEntity.getUpdatedAt())
                .deletedAt(jpaEntity.getDeletedAt())
                .build();
    }

    @Override
    public AddressJpaEntity toJpaEntity(Address domainEntity) {
        return AddressJpaEntity.builder()
                .id(domainEntity.getId())
                .userId(domainEntity.getUserId())
                .fullName(domainEntity.getFullName())
                .phone(domainEntity.getPhone())
                .street(domainEntity.getStreet())
                .city(domainEntity.getCity())
                .state(domainEntity.getState())
                .country(domainEntity.getCountry())
                .zipCode(domainEntity.getZipCode())
                .isDefault(domainEntity.isDefault())
                .createdAt(domainEntity.getCreatedAt())
                .updatedAt(domainEntity.getUpdatedAt())
                .deletedAt(domainEntity.getDeletedAt())
                .build();
    }
}
