package com.ravn.ecommerce.application.usecases.cart;

import com.ravn.ecommerce.application.repositories.CartRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.cart.Cart;
import com.ravn.ecommerce.domain.model.cart.CartStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClearCartUseCaseTest {

    private static final Long USER_ID = 1L;

    @Mock
    private CartRepository cartRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ClearCartUseCase clearCartUseCase;

    @Test
    @DisplayName("Should clear cart successfully")
    void shouldClearCartSuccessfully() {
        Cart cart = Cart.builder()
                .id(1L).userId(USER_ID).total(BigDecimal.ZERO)
                .status(CartStatus.ACTIVE).items(new ArrayList<>())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, USER_ID)).thenReturn(Optional.of(cart));

        clearCartUseCase.execute(USER_ID);

        verify(cartRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw UserNotFound when user does not exist")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.existsById(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> clearCartUseCase.execute(USER_ID))
                .isInstanceOf(UserNotFound.class);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no active cart")
    void shouldThrowWhenNoActiveCart() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clearCartUseCase.execute(USER_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
