package com.ravn.ecommerce.presentation.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.ravn.ecommerce.application.dto.response.OrderResponse;
import com.ravn.ecommerce.application.services.CurrentUserService;
import com.ravn.ecommerce.application.usecases.order.*;
import com.ravn.ecommerce.application.usecases.order.command.GetUserOrderByIdCommand;
import com.ravn.ecommerce.application.usecases.order.command.GetUserOrdersCommand;
import com.ravn.ecommerce.application.usecases.order.command.GetAllOrdersCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Window;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Endpoints for placing, retrieving, and managing orders")
public class OrderController {

    private final GetUserOrdersUseCase getUserOrdersUseCase;
    private final GetUserOrderByIdUseCase getUserOrderByIdUseCase;
    private final CancelUserOrderUseCase cancelUserOrderUseCase;
    private final GetAllOrdersUseCase getAllOrdersUseCase;
    private final GetOrderByIdUseCase getOrderByIdUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final CurrentUserService currentUserService;

    // ── User-scoped (CLIENT + MANAGER) ──────────────────────────────────────

    @Operation(summary = "Get user's orders", description = "Retrieves a paginated list of all orders placed by the currently authenticated user.")
    @GetMapping("/me")
    public ResponseEntity<Window<OrderResponse>> getMyOrders(
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(getUserOrdersUseCase.execute(
                new GetUserOrdersCommand(userId, ScrollPosition.keyset(), Limit.of(limit))));
    }

    @Operation(summary = "Get user's order by ID", description = "Retrieves details of a specific order placed by the currently authenticated user.")
    @GetMapping("/me/{orderId}")
    public ResponseEntity<OrderResponse> getMyOrderById(@PathVariable Long orderId) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(getUserOrderByIdUseCase.execute(new GetUserOrderByIdCommand(userId, orderId)));
    }

    @Operation(summary = "Cancel user's order", description = "Cancels a specific order placed by the currently authenticated user, provided it is in a cancellable state.")
    @PatchMapping("/me/{orderId}/cancel")
    public ResponseEntity<Void> cancelMyOrder(@PathVariable Long orderId) {
        Long userId = currentUserService.getCurrentUserId();
        cancelUserOrderUseCase.execute(new GetUserOrderByIdCommand(userId, orderId));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // ── Manager-only ─────────────────────────────────────────────────────────

    @Operation(summary = "Get all orders (Admin)", description = "Retrieves a paginated list of all orders in the system. Requires MANAGER role.")
    @GetMapping
    public ResponseEntity<Window<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(getAllOrdersUseCase.execute(
                new GetAllOrdersCommand(ScrollPosition.keyset(), Limit.of(limit))));
    }

    @Operation(summary = "Get order by ID (Admin)", description = "Retrieves details of any specific order by its ID. Requires MANAGER role.")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(getOrderByIdUseCase.execute(orderId));
    }

    @Operation(summary = "Cancel order (Admin)", description = "Cancels any specific order in the system. Requires MANAGER role.")
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        cancelOrderUseCase.execute(orderId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
