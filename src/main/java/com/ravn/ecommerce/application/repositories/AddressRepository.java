package com.ravn.ecommerce.application.repositories;

import com.ravn.ecommerce.domain.model.user.Address;

import java.util.List;
import java.util.Optional;

public interface AddressRepository {
    Address save(Address address);

    Optional<Address> findById(Long id);

    List<Address> findAllByUserId(Long userId);

    Optional<Address> findByIdAndUserId(Long id, Long userId);

    Optional<Address> findDefaultByUserId(Long userId);

    boolean existsById(Long id);

    List<Address> findAllById(List<Long> ids);

    void deleteById(Long id);
}
