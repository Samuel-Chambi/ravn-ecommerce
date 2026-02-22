package com.ravn.ecommerce.presentation.rest.controllers;

import com.ravn.ecommerce.application.dto.request.cart.AddToCartRequest;
import com.ravn.ecommerce.application.dto.request.cart.UpdateItemQuantityRequest;
import com.ravn.ecommerce.application.dto.response.CartResponse;
import com.ravn.ecommerce.application.useCases.cart.*;
import com.ravn.ecommerce.application.useCases.cart.command.AddItemToCartCommand;
import com.ravn.ecommerce.application.useCases.cart.command.RemoveItemFromCartCommand;
import com.ravn.ecommerce.application.useCases.cart.command.UpdateItemQuantityCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart/{userId}")
@RequiredArgsConstructor
public class CartController {
    private final GetCurrentUserCartUseCase getCurrentUserCartUseCase;
    private final AddItemToCartUseCase addItemToCartUseCase;
    private final ClearCartUseCase clearCartUseCase;
    private final UpdateItemQuantityUseCase updateItemQuantityUseCase;
    private final RemoveItemFromCartUseCase removeItemFromCartUseCase;
    @GetMapping
    public ResponseEntity<CartResponse> getCurrentUserCart(
            @PathVariable Long userId
    ){
        return ResponseEntity.status(HttpStatus.OK).body(getCurrentUserCartUseCase.execute(userId));
    }
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addProductToCart(
            @PathVariable Long userId,
            @RequestBody @Valid AddToCartRequest request
    ){
        return ResponseEntity.status(HttpStatus.OK).body(addItemToCartUseCase.execute(new AddItemToCartCommand(userId , request.getProductId(), request.getQuantity())));
    }
    @DeleteMapping
    public ResponseEntity<Void> clearCurrentUserCart(
            @PathVariable Long userId
    ){
        clearCartUseCase.execute(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestBody @Valid UpdateItemQuantityRequest request
    ){
        return ResponseEntity.status(HttpStatus.OK).body(updateItemQuantityUseCase.execute(new UpdateItemQuantityCommand(userId , productId, request.getQuantity())));
    }
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItemFromCart(
            @PathVariable Long userId,
            @PathVariable Long productId
    ){
        return ResponseEntity.status(HttpStatus.OK).body(removeItemFromCartUseCase.execute(new RemoveItemFromCartCommand(userId , productId)));
    }
}
