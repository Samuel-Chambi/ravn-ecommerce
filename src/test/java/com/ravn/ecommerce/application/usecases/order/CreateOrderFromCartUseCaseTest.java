package com.ravn.ecommerce.application.usecases.order;

import com.ravn.ecommerce.application.dto.response.OrderResponse;
import com.ravn.ecommerce.application.events.EventPublisher;
import com.ravn.ecommerce.application.repositories.*;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.InsufficientStockException;
import com.ravn.ecommerce.domain.exceptions.InvalidOrderException;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.cart.Cart;
import com.ravn.ecommerce.domain.model.cart.CartItem;
import com.ravn.ecommerce.domain.model.cart.CartStatus;
import com.ravn.ecommerce.domain.model.order.Order;
import com.ravn.ecommerce.domain.model.order.OrderStatus;
import com.ravn.ecommerce.domain.model.product.Inventory;
import com.ravn.ecommerce.domain.model.product.Product;
import com.ravn.ecommerce.domain.model.user.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderFromCartUseCaseTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private CreateOrderFromCartUseCase useCase;

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 10L;

    private Address buildDefaultAddress() {
        return Address.builder()
                .id(100L)
                .userId(USER_ID)
                .fullName("John Doe")
                .phone("123456789")
                .street("123 Main St")
                .city("Lima")
                .country("Peru")
                .zipCode("15001")
                .isDefault(true)
                .build();
    }

    private Product buildProduct(int stock) {
        return Product.builder()
                .id(PRODUCT_ID)
                .name("Test Product")
                .price(new BigDecimal("25.00"))
                .isEnabled(true)
                .inventory(Inventory.builder()
                        .id(1L)
                        .productId(PRODUCT_ID)
                        .quantity(stock)
                        .updatedAt(LocalDateTime.now())
                        .build())
                .build();
    }

    private Cart buildActiveCartWithItems() {
        CartItem item = CartItem.builder()
                .id(1L)
                .productId(PRODUCT_ID)
                .productName("Test Product")
                .price(new BigDecimal("25.00"))
                .quantity(2)
                .build();
        return Cart.builder()
                .id(1L)
                .userId(USER_ID)
                .status(CartStatus.ACTIVE)
                .total(new BigDecimal("50.00"))
                .items(List.of(item))
                .build();
    }

    @Test
    @DisplayName("Should create order successfully from cart")
    void shouldCreateOrderSuccessfully() {
        Cart cart = buildActiveCartWithItems();
        Product product = buildProduct(10);
        Address address = buildDefaultAddress();
        Order savedOrder = Order.builder()
                .id(1L)
                .userId(USER_ID)
                .status(OrderStatus.PENDING)
                .shippingAddressId(address.getId())
                .totalAmount(new BigDecimal("50.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(List.of())
                .build();

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(addressRepository.findDefaultByUserId(USER_ID)).thenReturn(Optional.of(address));
        when(cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, USER_ID)).thenReturn(Optional.of(cart));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        OrderResponse response = useCase.execute(USER_ID);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(USER_ID);
        verify(orderRepository).save(any(Order.class));
        verify(cartRepository).save(any(Cart.class));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw when user does not exist")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.existsById(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(USER_ID))
                .isInstanceOf(UserNotFound.class)
                .hasMessageContaining("does not exist");
    }

    @Test
    @DisplayName("Should throw when user has no default address")
    void shouldThrowWhenNoDefaultAddress() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(addressRepository.findDefaultByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(USER_ID))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("default shipping address");
    }

    @Test
    @DisplayName("Should throw when active cart not found")
    void shouldThrowWhenCartNotFound() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(addressRepository.findDefaultByUserId(USER_ID)).thenReturn(Optional.of(buildDefaultAddress()));
        when(cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(USER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Active cart");
    }

    @Test
    @DisplayName("Should throw when cart is empty")
    void shouldThrowWhenCartIsEmpty() {
        Cart emptyCart = Cart.builder()
                .id(1L)
                .userId(USER_ID)
                .status(CartStatus.ACTIVE)
                .items(List.of())
                .build();

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(addressRepository.findDefaultByUserId(USER_ID)).thenReturn(Optional.of(buildDefaultAddress()));
        when(cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, USER_ID)).thenReturn(Optional.of(emptyCart));

        assertThatThrownBy(() -> useCase.execute(USER_ID))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("empty cart");
    }

    @Test
    @DisplayName("Should throw when product has insufficient stock")
    void shouldThrowWhenInsufficientStock() {
        Cart cart = buildActiveCartWithItems(); // quantity = 2
        Product product = buildProduct(1); // stock = 1

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(addressRepository.findDefaultByUserId(USER_ID)).thenReturn(Optional.of(buildDefaultAddress()));
        when(cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, USER_ID)).thenReturn(Optional.of(cart));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> useCase.execute(USER_ID))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    @DisplayName("Should throw when product is disabled")
    void shouldThrowWhenProductDisabled() {
        Cart cart = buildActiveCartWithItems();
        Product disabledProduct = buildProduct(10);
        disabledProduct.setEnabled(false);

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(addressRepository.findDefaultByUserId(USER_ID)).thenReturn(Optional.of(buildDefaultAddress()));
        when(cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, USER_ID)).thenReturn(Optional.of(cart));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(disabledProduct));

        assertThatThrownBy(() -> useCase.execute(USER_ID))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("not available");
    }
}
