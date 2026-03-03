package com.ravn.ecommerce.application.usecases.cart;

import com.ravn.ecommerce.application.dto.response.CartResponse;
import com.ravn.ecommerce.application.repositories.CartRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.usecases.cart.command.UpdateItemQuantityCommand;
import com.ravn.ecommerce.domain.exceptions.BusinessRuleViolation;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.InsufficientStockException;
import com.ravn.ecommerce.domain.model.cart.Cart;
import com.ravn.ecommerce.domain.model.cart.CartItem;
import com.ravn.ecommerce.domain.model.cart.CartStatus;
import com.ravn.ecommerce.domain.model.product.Inventory;
import com.ravn.ecommerce.domain.model.product.Product;
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
class UpdateItemQuantityUseCaseTest {

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 10L;

    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private UpdateItemQuantityUseCase updateItemQuantityUseCase;

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

    private Product buildEnabledProduct(int stock) {
        return Product.builder()
                .id(PRODUCT_ID)
                .name("Test Product")
                .price(new BigDecimal("19.99"))
                .isEnabled(true)
                .inventory(Inventory.builder().productId(PRODUCT_ID).quantity(stock).updatedAt(LocalDateTime.now()).build())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should update item quantity successfully")
    void shouldUpdateQuantitySuccessfully() {
        UpdateItemQuantityCommand command = new UpdateItemQuantityCommand(USER_ID, PRODUCT_ID, 3);
        Cart cart = buildCartWithItem();

        when(cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, USER_ID)).thenReturn(Optional.of(cart));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(buildEnabledProduct(10)));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

        CartResponse response = updateItemQuantityUseCase.execute(command);

        assertThat(response).isNotNull();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no active cart")
    void shouldThrowWhenNoActiveCart() {
        UpdateItemQuantityCommand command = new UpdateItemQuantityCommand(USER_ID, PRODUCT_ID, 3);
        when(cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateItemQuantityUseCase.execute(command))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw BusinessRuleViolation when product not in cart")
    void shouldThrowWhenProductNotInCart() {
        UpdateItemQuantityCommand command = new UpdateItemQuantityCommand(USER_ID, 999L, 3);
        Cart cart = buildCartWithItem();
        when(cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, USER_ID)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> updateItemQuantityUseCase.execute(command))
                .isInstanceOf(BusinessRuleViolation.class);
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when stock is insufficient")
    void shouldThrowWhenInsufficientStock() {
        UpdateItemQuantityCommand command = new UpdateItemQuantityCommand(USER_ID, PRODUCT_ID, 15);
        Cart cart = buildCartWithItem();

        when(cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, USER_ID)).thenReturn(Optional.of(cart));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(buildEnabledProduct(5)));

        assertThatThrownBy(() -> updateItemQuantityUseCase.execute(command))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    @DisplayName("Should throw BusinessRuleViolation when product is disabled")
    void shouldThrowWhenProductDisabled() {
        UpdateItemQuantityCommand command = new UpdateItemQuantityCommand(USER_ID, PRODUCT_ID, 3);
        Cart cart = buildCartWithItem();
        Product disabledProduct = buildEnabledProduct(10);
        disabledProduct.disable();

        when(cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, USER_ID)).thenReturn(Optional.of(cart));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(disabledProduct));

        assertThatThrownBy(() -> updateItemQuantityUseCase.execute(command))
                .isInstanceOf(BusinessRuleViolation.class)
                .hasMessageContaining("not enabled");
    }
}
