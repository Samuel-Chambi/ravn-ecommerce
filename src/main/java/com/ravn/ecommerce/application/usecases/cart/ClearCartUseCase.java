package com.ravn.ecommerce.application.usecases.cart;

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
public class ClearCartUseCase implements UseCase<Long, Void> {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    @Override
    public Void execute(Long userId){
        log.info("Deleting user ID {} cart" , userId);
        if(!userRepository.existsById(userId)){
            throw new UserNotFound(String.format("User ID %d does not exist" , userId));
        }
        Cart currentCart = cartRepository.findByStatusAndUserId(CartStatus.ACTIVE , userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Active cart for user ID %d does not exist" , userId)));
        cartRepository.deleteById(currentCart.getId());
        log.info("User ID {} cart deleted successfully" , userId);
        return null;
    }
}
