package com.ravn.ecommerce.presentation.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "Endpoints for managing the user's shopping cart")
public class CartController {

    private final GetCurrentUserCartUseCase getCurrentUserCartUseCase;
    private final AddItemToCartUseCase addItemToCartUseCase;
    private final ClearCartUseCase clearCartUseCase;
    private final UpdateItemQuantityUseCase updateItemQuantityUseCase;
    private final RemoveItemFromCartUseCase removeItemFromCartUseCase;
    private final CreateOrderFromCartUseCase createOrderFromCartUseCase;
    private final CurrentUserService currentUserService;

    @Operation(summary = "Get current user's cart", description = "Retrieves the active shopping cart and its items for the currently authenticated user.")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> getCurrentUserCart() {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(getCurrentUserCartUseCase.execute(userId));
    }

    @Operation(summary = "Add item to cart", description = "Adds a specific quantity of a product to the user's shopping cart.")
    @PostMapping("/items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> addProductToCart(
            @RequestBody @Valid AddToCartRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(addItemToCartUseCase.execute(
                new AddItemToCartCommand(userId, request.getProductId(), request.getQuantity())));
    }

    @Operation(summary = "Checkout cart", description = "Converts the current shopping cart into a new order and clears the cart.")
    @PostMapping("/check-out")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> createOrderFromCart() {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(createOrderFromCartUseCase.execute(userId));
    }

    @Operation(summary = "Clear cart", description = "Removes all items from the current user's shopping cart.")
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> clearCurrentUserCart() {
        Long userId = currentUserService.getCurrentUserId();
        clearCartUseCase.execute(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Update item quantity", description = "Updates the quantity of a specific product already in the shopping cart.")
    @PutMapping("/items/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable Long productId,
            @RequestBody @Valid UpdateItemQuantityRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(updateItemQuantityUseCase.execute(
                new UpdateItemQuantityCommand(userId, productId, request.getQuantity())));
    }

    @Operation(summary = "Remove item from cart", description = "Removes a specific product entirely from the shopping cart.")
    @DeleteMapping("/items/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CartResponse> removeItemFromCart(@PathVariable Long productId) {
        Long userId = currentUserService.getCurrentUserId();
        return ResponseEntity.ok(removeItemFromCartUseCase.execute(
                new RemoveItemFromCartCommand(userId, productId)));
    }
}
