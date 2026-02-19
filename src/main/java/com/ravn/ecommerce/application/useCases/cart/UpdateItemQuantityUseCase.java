package com.ravn.ecommerce.application.usecases.cart;

import com.ravn.ecommerce.application.dto.response.CartResponse;
import com.ravn.ecommerce.application.repositories.CartItemRepository;
import com.ravn.ecommerce.application.repositories.CartRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.usecases.cart.command.UpdateItemQuantityCommand;
import com.ravn.ecommerce.domain.exceptions.BusinessRuleViolation;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.model.cart.Cart;
import com.ravn.ecommerce.domain.model.cart.CartStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateItemQuantityUseCase implements UseCase<UpdateItemQuantityCommand, CartResponse> {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    @Override
    public CartResponse execute(UpdateItemQuantityCommand command){
        Long productId = command.productId();
        Long userId = command.userId();
        Integer quantity = command.quantity();
        log.info("Updating quantity for product ID {} on user ID {} cart" , productId , userId);
        Cart currentCart = cartRepository.findByStatusAndUserId(CartStatus.ACTIVE , userId).
                orElseThrow(() -> new EntityNotFoundException(String.format("Active cart for user ID %d does not exist" , userId)));
        if(!cartItemRepository.existsByProductIdAndCartId(productId, currentCart.getId())){
            throw new BusinessRuleViolation(String.format("Product ID %d is not present on user ID %d cart" , productId , userId));
        }

        currentCart.updateItemQuantity(productId, quantity);
        Cart saved = cartRepository.save(currentCart);
        return CartResponse.toDto(saved);
    }
}
