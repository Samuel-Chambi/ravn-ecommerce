package com.ravn.ecommerce.application.usecases.cart;

import com.ravn.ecommerce.application.dto.response.CartResponse;
import com.ravn.ecommerce.application.repositories.CartRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.usecases.cart.command.RemoveItemFromCartCommand;
import com.ravn.ecommerce.domain.exceptions.BusinessRuleViolation;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.model.cart.Cart;
import com.ravn.ecommerce.domain.model.cart.CartItem;
import com.ravn.ecommerce.domain.model.cart.CartStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RemoveItemFromCartUseCase implements UseCase<RemoveItemFromCartCommand, CartResponse> {
    private final CartRepository cartRepository;

    @Override
    public CartResponse execute(RemoveItemFromCartCommand command) {
        Long userId = command.userId();
        Long productId = command.productId();

        Cart currentCart = cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Active cart for user ID %d does not exist", userId)));
        CartItem currentItem = currentCart.findItemByProductId(productId)
                .orElseThrow(() -> new BusinessRuleViolation(
                        String.format("Product ID %d is not present on user ID %d cart", productId, userId)));

        currentCart.removeItem(productId);
        Cart saved = cartRepository.save(currentCart);
        return CartResponse.toDto(saved);
    }
}
