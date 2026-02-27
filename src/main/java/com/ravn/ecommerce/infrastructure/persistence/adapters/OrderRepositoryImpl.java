package com.ravn.ecommerce.infrastructure.persistence.adapters;

import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.domain.model.order.Order;
import com.ravn.ecommerce.infrastructure.persistence.jpa.entity.OrderJpaEntity;
import com.ravn.ecommerce.infrastructure.persistence.jpa.mapper.OrderMapper;
import com.ravn.ecommerce.infrastructure.persistence.jpa.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Window;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderJpaRepository orderJpaRepository;
    private final OrderMapper orderMapper;

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = orderMapper.toJpaEntity(order);
        OrderJpaEntity saved = orderJpaRepository.save(entity);
        return orderMapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(Long orderId) {
        return orderJpaRepository.findById(orderId).map(orderMapper::toDomain);
    }

    @Override
    public Optional<Order> findByIdAndUserId(Long orderId, Long userId) {
        return orderJpaRepository.findByIdAndUserId(orderId, userId).map(orderMapper::toDomain);
    }

    @Override
    public List<Order> findAllByUserId(Long userId) {
        return orderJpaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(orderMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Window<Order> findAllByUserId(Long userId, ScrollPosition position, Limit limit) {
        return orderJpaRepository.findByUserIdOrderByCreatedAtDesc(userId, position, limit)
                .map(orderMapper::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return orderJpaRepository.findAll().stream()
                .map(orderMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Window<Order> findAll(ScrollPosition position, Limit limit) {
        return orderJpaRepository.findAllByOrderByCreatedAtDesc(position, limit)
                .map(orderMapper::toDomain);
    }

    @Override
    public boolean existsById(Long orderId) {
        return orderJpaRepository.existsById(orderId);
    }

    @Override
    public void deleteById(Long orderId) {
        orderJpaRepository.deleteById(orderId);
    }
}
