package com.ravn.ecommerce.presentation.rest.controllers;

import com.ravn.ecommerce.application.dto.response.OrderResponse;
import com.ravn.ecommerce.application.useCases.order.*;
import com.ravn.ecommerce.application.useCases.order.command.GetUserOrderByIdCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController {
    private final GetUserOrdersUseCase getUserOrdersUseCase;
    private final GetUserOrderByIdUseCase getUserOrderByIdUseCase;
    private final CancelUserOrderUseCase cancelUserOrderUseCase;
    private final GetAllOrdersUseCase getAllOrdersUseCase;
    private final GetOrderByIdUseCase getOrderByIdUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;



    @GetMapping("/users/{userId}/orders")
    public ResponseEntity<List<OrderResponse>> getUserOrders(@PathVariable Long userId) {
        return ResponseEntity.ok(getUserOrdersUseCase.execute(userId));
    }

    @GetMapping("/users/{userId}/orders/{orderId}")
    public ResponseEntity<OrderResponse> getUserOrderById(
            @PathVariable Long userId,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(getUserOrderByIdUseCase.execute(new GetUserOrderByIdCommand(userId, orderId)));
    }

    @PatchMapping("/users/{userId}/orders/{orderId}")
    public ResponseEntity<Void> cancelUserOrder(
            @PathVariable Long userId,
            @PathVariable Long orderId) {
        cancelUserOrderUseCase.execute(new GetUserOrderByIdCommand(userId, orderId));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(getAllOrdersUseCase.execute(null));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(getOrderByIdUseCase.execute(orderId));
    }

    @PatchMapping("/orders/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        cancelOrderUseCase.execute(orderId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
