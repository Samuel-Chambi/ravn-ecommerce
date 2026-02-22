package com.ravn.ecommerce.application.usecases.cart;

import com.ravn.ecommerce.application.dto.response.CartResponse;
import com.ravn.ecommerce.application.repositories.CartItemRepository;
import com.ravn.ecommerce.application.repositories.CartRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.usecases.cart.command.RemoveItemFromCartCommand;
import com.ravn.ecommerce.domain.exceptions.BusinessRuleViolation;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.ProductNotFound;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.cart.Cart;
import com.ravn.ecommerce.domain.model.cart.CartStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RemoveItemFromCartUseCase implements UseCase<RemoveItemFromCartCommand, CartResponse>{
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public CartResponse execute(RemoveItemFromCartCommand command){
        Long userId = command.userId();
        Long productId = command.productId();
        if(!userRepository.existsById(userId)){
            throw new UserNotFound(String.format("User ID %d does not exist" , userId));
        }
        if(!productRepository.existsById(productId)){
            throw new ProductNotFound(String.format("Product ID %d does not exist" , productId));
        }
        Cart currentCart = cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Active cart for user ID %d does not exist", userId)));
        if(!cartItemRepository.existsByProductIdAndCartId(productId , currentCart.getId())){
            throw new BusinessRuleViolation(String.format("Product ID %d is not present on user ID %d cart" , productId , userId));
        }

        currentCart.removeItem(productId);
        Cart saved = cartRepository.save(currentCart);
        return CartResponse.toDto(saved);
    }
}
