package com.ravn.ecommerce.application.useCases.cart.command;

import com.ravn.ecommerce.domain.exceptions.InvalidCartLogicException;

public record UpdateItemQuantityCommand(
        Long userId,
        Long productId,
        Integer quantity
) {
    public UpdateItemQuantityCommand{
        if(quantity < 1){
            throw new InvalidCartLogicException("Quantity value must be positive");
        }
    }
}
