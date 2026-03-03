package com.ravn.ecommerce.application.usecases.cart;

import com.ravn.ecommerce.application.dto.response.CartResponse;
import com.ravn.ecommerce.application.repositories.CartRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.cart.command.AddItemToCartCommand;
import com.ravn.ecommerce.domain.exceptions.BusinessRuleViolation;
import com.ravn.ecommerce.domain.exceptions.InsufficientStockException;
import com.ravn.ecommerce.domain.exceptions.ProductNotFound;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
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
class AddItemToCartUseCaseTest {

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 10L;

    @Mock
    private CartRepository cartRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private AddItemToCartUseCase addItemToCartUseCase;

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

    private Cart buildActiveCart() {
        return Cart.builder()
                .id(1L)
                .userId(USER_ID)
                .total(BigDecimal.ZERO)
                .status(CartStatus.ACTIVE)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should add item to existing cart successfully")
    void shouldAddItemToExistingCart() {
        AddItemToCartCommand command = new AddItemToCartCommand(USER_ID, PRODUCT_ID, 2);
        Cart cart = buildActiveCart();

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(buildEnabledProduct(10)));
        when(cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, USER_ID)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

        CartResponse response = addItemToCartUseCase.execute(command);

        assertThat(response).isNotNull();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should create new cart when no active cart exists")
    void shouldCreateNewCartWhenNoneExists() {
        AddItemToCartCommand command = new AddItemToCartCommand(USER_ID, PRODUCT_ID, 1);
        Cart newCart = buildActiveCart();

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(buildEnabledProduct(10)));
        when(cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, USER_ID)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        CartResponse response = addItemToCartUseCase.execute(command);

        assertThat(response).isNotNull();
        verify(cartRepository, atLeast(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should throw UserNotFound when user does not exist")
    void shouldThrowWhenUserNotFound() {
        AddItemToCartCommand command = new AddItemToCartCommand(USER_ID, PRODUCT_ID, 1);
        when(userRepository.existsById(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> addItemToCartUseCase.execute(command))
                .isInstanceOf(UserNotFound.class);
    }

    @Test
    @DisplayName("Should throw ProductNotFound when product does not exist")
    void shouldThrowWhenProductNotFound() {
        AddItemToCartCommand command = new AddItemToCartCommand(USER_ID, PRODUCT_ID, 1);
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addItemToCartUseCase.execute(command))
                .isInstanceOf(ProductNotFound.class);
    }

    @Test
    @DisplayName("Should throw BusinessRuleViolation when product is disabled")
    void shouldThrowWhenProductDisabled() {
        AddItemToCartCommand command = new AddItemToCartCommand(USER_ID, PRODUCT_ID, 1);
        Product disabledProduct = buildEnabledProduct(10);
        disabledProduct.disable();

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(disabledProduct));

        assertThatThrownBy(() -> addItemToCartUseCase.execute(command))
                .isInstanceOf(BusinessRuleViolation.class)
                .hasMessageContaining("not enabled");
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when stock is insufficient")
    void shouldThrowWhenInsufficientStock() {
        AddItemToCartCommand command = new AddItemToCartCommand(USER_ID, PRODUCT_ID, 5);
        Cart cart = buildActiveCart();

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(buildEnabledProduct(3)));
        when(cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, USER_ID)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> addItemToCartUseCase.execute(command))
                .isInstanceOf(InsufficientStockException.class);
    }
}
