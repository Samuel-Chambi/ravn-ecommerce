package com.ravn.ecommerce.application.usecases.cart;

import com.ravn.ecommerce.application.dto.response.CartResponse;
import com.ravn.ecommerce.application.repositories.CartRepository;
import com.ravn.ecommerce.application.usecases.cart.command.RemoveItemFromCartCommand;
import com.ravn.ecommerce.domain.exceptions.BusinessRuleViolation;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.model.cart.Cart;
import com.ravn.ecommerce.domain.model.cart.CartItem;
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
class RemoveItemFromCartUseCaseTest {

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 10L;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private RemoveItemFromCartUseCase removeItemFromCartUseCase;

    private Cart buildCartWithItem() {
        Cart cart = Cart.builder()
                .id(1L)
                .userId(USER_ID)
                .total(BigDecimal.ZERO)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        CartItem item = CartItem.builder()
                .id(1L)
                .cartId(1L)
                .productId(PRODUCT_ID)
                .productName("Test Product")
                .price(new BigDecimal("19.99"))
                .quantity(2)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        cart.addItem(item);
        return cart;
    }

    @Test
    @DisplayName("Should remove item from cart successfully")
    void shouldRemoveItemSuccessfully() {
        RemoveItemFromCartCommand command = new RemoveItemFromCartCommand(USER_ID, PRODUCT_ID);
        Cart cart = buildCartWithItem();

        when(cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, USER_ID)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

        CartResponse response = removeItemFromCartUseCase.execute(command);

        assertThat(response).isNotNull();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no active cart exists")
    void shouldThrowWhenNoActiveCart() {
        RemoveItemFromCartCommand command = new RemoveItemFromCartCommand(USER_ID, PRODUCT_ID);
        when(cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> removeItemFromCartUseCase.execute(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Active cart");
    }

    @Test
    @DisplayName("Should throw BusinessRuleViolation when product not in cart")
    void shouldThrowWhenProductNotInCart() {
        RemoveItemFromCartCommand command = new RemoveItemFromCartCommand(USER_ID, 999L);
        Cart cart = buildCartWithItem();

        when(cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, USER_ID)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> removeItemFromCartUseCase.execute(command))
                .isInstanceOf(BusinessRuleViolation.class)
                .hasMessageContaining("not present");
    }
}
