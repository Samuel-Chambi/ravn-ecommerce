package com.ravn.ecommerce.application.usecases.cart;

import com.ravn.ecommerce.application.dto.response.CartResponse;
import com.ravn.ecommerce.application.repositories.CartRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCurrentUserCartUseCaseTest {

    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;
    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private GetCurrentUserCartUseCase getCurrentUserCartUseCase;

    @Test
    @DisplayName("Should return existing active cart")
    void shouldReturnExistingActiveCart() {
        Cart cart = Cart.builder()
                .id(1L).userId(USER_ID).total(new BigDecimal("39.98"))
                .status(CartStatus.ACTIVE).items(new ArrayList<>())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, USER_ID)).thenReturn(Optional.of(cart));

        CartResponse response = getCurrentUserCartUseCase.execute(USER_ID);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(USER_ID);
        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create new cart when no active cart exists")
    void shouldCreateNewCartWhenNoneExists() {
        Cart newCart = Cart.builder()
                .id(2L).userId(USER_ID).total(BigDecimal.ZERO)
                .status(CartStatus.ACTIVE).items(new ArrayList<>())
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, USER_ID)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        CartResponse response = getCurrentUserCartUseCase.execute(USER_ID);

        assertThat(response).isNotNull();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should throw UserNotFound when user does not exist")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.existsById(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> getCurrentUserCartUseCase.execute(USER_ID))
                .isInstanceOf(UserNotFound.class);
    }
}
