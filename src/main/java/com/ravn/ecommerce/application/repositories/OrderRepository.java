package com.ravn.ecommerce.application.repositories;

import com.ravn.ecommerce.domain.model.order.Order;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Window;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);

    Optional<Order> findById(Long orderId);

    Optional<Order> findByIdAndUserId(Long orderId, Long userId);

    List<Order> findAllByUserId(Long userId);

    Window<Order> findAllByUserId(Long userId, ScrollPosition position, Limit limit);

    List<Order> findAll();

    Window<Order> findAll(ScrollPosition position, Limit limit);

    boolean existsById(Long orderId);

    void deleteById(Long orderId);
}
