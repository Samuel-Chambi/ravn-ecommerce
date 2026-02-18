package com.ravn.ecommerce.application.repositories;

import com.ravn.ecommerce.domain.model.order.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);

    Optional<Order> findById(Long orderId);

    Optional<Order> findByIdAndUserId(Long orderId, Long userId);

    List<Order> findAllByUserId(Long userId);

    List<Order> findAll();

    boolean existsById(Long orderId);

    void deleteById(Long orderId);
}
