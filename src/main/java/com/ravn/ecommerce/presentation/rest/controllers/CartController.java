package com.ravn.ecommerce.presentation.rest.controllers;

import com.ravn.ecommerce.application.dto.request.cart.AddToCartRequest;
import com.ravn.ecommerce.application.dto.request.cart.UpdateItemQuantityRequest;
import com.ravn.ecommerce.application.dto.response.CartResponse;
import com.ravn.ecommerce.application.dto.response.OrderResponse;
import com.ravn.ecommerce.application.services.CurrentUserService;
import com.ravn.ecommerce.application.usecases.cart.*;
import com.ravn.ecommerce.application.usecases.cart.command.AddItemToCartCommand;
import com.ravn.ecommerce.application.usecases.cart.command.RemoveItemFromCartCommand;
import com.ravn.ecommerce.application.usecases.cart.command.UpdateItemQuantityCommand;
import com.ravn.ecommerce.application.usecases.order.CreateOrderFromCartUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final GetCurrentUserCartUseCase getCurrentUserCartUseCase;
    private final AddItemToCartUseCase addItemToCartUseCase;
    private final ClearCartUseCase clearCartUseCase;
    private final UpdateItemQuantityUseCase updateItemQuantityUseCase;
    private final RemoveItemFromCartUseCase removeItemFromCartUseCase;
    private final CreateOrderFromCartUseCase createOrderFromCartUseCase;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<CartResponse> getCurrentUserCart() {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(getCurrentUserCartUseCase.execute(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addProductToCart(
            @RequestBody @Valid AddToCartRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(addItemToCartUseCase.execute(
                new AddItemToCartCommand(userId, request.getProductId(), request.getQuantity())));
    }

    @PostMapping("/check-out")
    public ResponseEntity<OrderResponse> createOrderFromCart() {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(createOrderFromCartUseCase.execute(userId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCurrentUserCart() {
        Long userId = currentUserService.getCurrentUserId();
        clearCartUseCase.execute(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable Long productId,
            @RequestBody @Valid UpdateItemQuantityRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(updateItemQuantityUseCase.execute(
                new UpdateItemQuantityCommand(userId, productId, request.getQuantity())));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItemFromCart(@PathVariable Long productId) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(removeItemFromCartUseCase.execute(
                new RemoveItemFromCartCommand(userId, productId)));
    }
}
