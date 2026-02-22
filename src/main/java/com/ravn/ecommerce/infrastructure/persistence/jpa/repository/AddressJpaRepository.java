package com.ravn.ecommerce.infrastructure.persistence.jpa.repository;

import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.AddressJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface AddressJpaRepository extends JpaRepository<AddressJpaEntity, Long> {
    List<AddressJpaEntity> findByUserId(Long userId);

    Optional<AddressJpaEntity> findByIdAndUserId(Long id, Long userId);

    Optional<AddressJpaEntity> findByUserIdAndIsDefaultTrue(Long userId);
}
