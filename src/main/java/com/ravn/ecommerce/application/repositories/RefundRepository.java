package com.ravn.ecommerce.application.repositories;

import com.ravn.ecommerce.domain.model.order.Refund;
import com.ravn.ecommerce.domain.model.order.RefundStatus;

import java.util.List;
import java.util.Optional;

public interface RefundRepository {
    Refund save(Refund refund);

    Optional<Refund> findById(Long id);

    List<Refund> findByUserId(Long userId);

    List<Refund> findByStatus(RefundStatus status);

    boolean existsActiveByOrderId(Long orderId);
}
