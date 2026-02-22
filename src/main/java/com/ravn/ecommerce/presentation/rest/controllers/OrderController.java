package com.ravn.ecommerce.presentation.rest.controllers;

import com.ravn.ecommerce.application.dto.response.OrderResponse;
import com.ravn.ecommerce.application.services.CurrentUserService;
import com.ravn.ecommerce.application.usecases.order.*;
import com.ravn.ecommerce.application.usecases.order.command.GetUserOrderByIdCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final GetUserOrdersUseCase getUserOrdersUseCase;
    private final GetUserOrderByIdUseCase getUserOrderByIdUseCase;
    private final CancelUserOrderUseCase cancelUserOrderUseCase;
    private final GetAllOrdersUseCase getAllOrdersUseCase;
    private final GetOrderByIdUseCase getOrderByIdUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final CurrentUserService currentUserService;

    // ── User-scoped (CLIENT + MANAGER) ──────────────────────────────────────

    @GetMapping("/me")
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(getUserOrdersUseCase.execute(userId));
    }

    @GetMapping("/me/{orderId}")
    public ResponseEntity<OrderResponse> getMyOrderById(@PathVariable Long orderId) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(getUserOrderByIdUseCase.execute(new GetUserOrderByIdCommand(userId, orderId)));
    }

    @PatchMapping("/me/{orderId}/cancel")
    public ResponseEntity<Void> cancelMyOrder(@PathVariable Long orderId) {
        Long userId = currentUserService.getCurrentUserId();
        cancelUserOrderUseCase.execute(new GetUserOrderByIdCommand(userId, orderId));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // ── Manager-only ─────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(getAllOrdersUseCase.execute(null));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(getOrderByIdUseCase.execute(orderId));
    }

    @PatchMapping("/orders/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        cancelOrderUseCase.execute(orderId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
