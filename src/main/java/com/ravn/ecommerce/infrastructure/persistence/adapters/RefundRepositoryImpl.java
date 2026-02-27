package com.ravn.ecommerce.infrastructure.persistence.adapters;

import com.ravn.ecommerce.domain.model.order.Refund;
import com.ravn.ecommerce.domain.model.order.RefundStatus;
import com.ravn.ecommerce.application.repositories.RefundRepository;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.RefundJpaEntity;
import com.ravn.ecommerce.infrastructure.persistence.jpa.mapper.RefundMapper;
import com.ravn.ecommerce.infrastructure.persistence.jpa.repository.RefundJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefundRepositoryImpl implements RefundRepository {

    private final RefundJpaRepository repository;
    private final RefundMapper mapper;

    @Override
    public Refund save(Refund refund) {
        RefundJpaEntity entity = mapper.toJpaEntity(refund);
        RefundJpaEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Refund> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Refund> findByUserId(Long userId) {
        return repository.findByUserId(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Refund> findByStatus(RefundStatus status) {
        int statusInt = switch (status) {
            case APPROVED -> 2;
            case REJECTED -> 3;
            default -> 1;
        };
        return repository.findByStatus(statusInt).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsActiveByOrderId(Long orderId) {
        // PENDING = 1, APPROVED = 2
        return repository.existsByOrderIdAndStatusIn(orderId, List.of(1, 2));
    }
}
