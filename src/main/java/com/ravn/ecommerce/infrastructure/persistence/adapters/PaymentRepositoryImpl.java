package com.ravn.ecommerce.infrastructure.persistence.adapters;

import com.ravn.ecommerce.application.repositories.PaymentRepository;
import com.ravn.ecommerce.domain.model.payment.Payment;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.PaymentJpaEntity;
import com.ravn.ecommerce.infrastructure.persistence.jpa.mapper.PaymentMapper;
import com.ravn.ecommerce.infrastructure.persistence.jpa.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;
    private final PaymentMapper mapper;

    @Override
    @Transactional
    public Payment save(Payment payment) {
        PaymentJpaEntity saved = jpaRepository.save(mapper.toJpaEntity(payment));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Payment> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId).map(mapper::toDomain);
    }

    @Override
    public Optional<Payment> findByStripePaymentIntent(String stripePaymentIntent) {
        return jpaRepository.findByStripePaymentIntent(stripePaymentIntent).map(mapper::toDomain);
    }

    @Override
    public boolean existsByOrderId(Long orderId) {
        return jpaRepository.existsByOrderId(orderId);
    }
}
