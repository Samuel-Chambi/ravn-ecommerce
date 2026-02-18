package com.ravn.ecommerce.presentation.rest.controllers;

import com.ravn.ecommerce.application.dto.request.AddToCartRequest;
import com.ravn.ecommerce.application.dto.response.CartResponse;
import com.ravn.ecommerce.application.useCases.cart.AddItemToCartUseCase;
import com.ravn.ecommerce.application.useCases.cart.command.AddItemToCartCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart/{userId}")
@RequiredArgsConstructor
public class CartController {
    private final AddItemToCartUseCase addItemToCartUseCase;
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addProductToCart(
            @PathVariable Long userId,
            @RequestBody @Valid AddToCartRequest request
    ){
        return ResponseEntity.status(HttpStatus.OK).body(addItemToCartUseCase.execute(new AddItemToCartCommand(userId , request.getProductId(), request.getQuantity())));
    }
}
