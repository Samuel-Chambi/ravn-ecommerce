package com.ravn.ecommerce.infrastructure.persistence.jpa.repository;

import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

}
