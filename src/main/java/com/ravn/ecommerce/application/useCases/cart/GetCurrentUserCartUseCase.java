package com.ravn.ecommerce.application.usecases.cart;

import com.ravn.ecommerce.application.dto.response.CartResponse;
import com.ravn.ecommerce.application.repositories.CartRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.cart.Cart;
import com.ravn.ecommerce.domain.model.cart.CartStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetCurrentUserCartUseCase implements UseCase<Long, CartResponse>{
    private final UserRepository userRepository;
    private final CartRepository cartRepository;

    @Override
    public CartResponse execute(Long userId){
        log.info("Fetching current cart for User ID: {}" , userId);
        if(!userRepository.existsById(userId)){
            throw new UserNotFound(String.format("User ID %d does not exist" , userId));
        }
        /*
            TODO: Consider the case when current user has not an active cart
               -> 1: Create a new empty cart an return it
               -> 2: Return an exception
        */
        Cart currentCart = cartRepository.findByStatusAndUserId(CartStatus.ACTIVE , userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Active cart for user ID %d does not exist" , userId)));

        return CartResponse.toDto(currentCart);
    }
}
