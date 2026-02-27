package com.ravn.ecommerce.infrastructure.persistence.adapters;

import com.ravn.ecommerce.application.repositories.AddressRepository;
import com.ravn.ecommerce.domain.model.user.Address;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.AddressJpaEntity;
import com.ravn.ecommerce.infrastructure.persistence.jpa.mapper.AddressMapper;
import com.ravn.ecommerce.infrastructure.persistence.jpa.repository.AddressJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AddressRepositoryImpl implements AddressRepository {
    private final AddressJpaRepository addressJpaRepository;
    private final AddressMapper addressMapper;

    @Override
    public Address save(Address address) {
        AddressJpaEntity entity = addressMapper.toJpaEntity(address);
        return addressMapper.toDomain(addressJpaRepository.save(entity));
    }

    @Override
    public Optional<Address> findById(Long id) {
        return addressJpaRepository.findById(id).map(addressMapper::toDomain);
    }

    @Override
    public List<Address> findAllByUserId(Long userId) {
        return addressJpaRepository.findByUserId(userId).stream()
                .map(addressMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Address> findByIdAndUserId(Long id, Long userId) {
        return addressJpaRepository.findByIdAndUserId(id, userId).map(addressMapper::toDomain);
    }

    @Override
    public Optional<Address> findDefaultByUserId(Long userId) {
        return addressJpaRepository.findByUserIdAndIsDefaultTrue(userId).map(addressMapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return addressJpaRepository.existsById(id);
    }

    @Override
    public List<Address> findAllById(List<Long> ids) {
        return addressJpaRepository.findAllById(ids).stream()
                .map(addressMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        addressJpaRepository.deleteById(id);
    }
}
