package com.ravn.ecommerce.application.usecases.product;

import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.product.command.DeleteProductCommand;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.UnauthorizedException;
import com.ravn.ecommerce.domain.model.product.Product;
import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteProductUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private DeleteProductUseCase useCase;

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 10L;

    private User buildUser(UserRole role) {
        return User.builder()
                .id(USER_ID)
                .email("manager@test.com")
                .passwordHash("hashed")
                .role(role)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Product buildProduct() {
        return Product.builder()
                .id(PRODUCT_ID)
                .name("Test Product")
                .price(new BigDecimal("25.00"))
                .isEnabled(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should delete product successfully when user is MANAGER")
    void shouldDeleteProductSuccessfully() {
        User manager = buildUser(UserRole.MANAGER);
        Product product = buildProduct();
        DeleteProductCommand command = new DeleteProductCommand(PRODUCT_ID, USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(manager));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        useCase.execute(command);

        verify(productRepository).deleteById(PRODUCT_ID);
    }

    @Test
    @DisplayName("Should throw when user does not exist")
    void shouldThrowWhenUserNotFound() {
        DeleteProductCommand command = new DeleteProductCommand(PRODUCT_ID, USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should throw when user is not MANAGER")
    void shouldThrowWhenUserNotManager() {
        User client = buildUser(UserRole.CLIENT);
        DeleteProductCommand command = new DeleteProductCommand(PRODUCT_ID, USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("managers");
    }

    @Test
    @DisplayName("Should throw when product does not exist")
    void shouldThrowWhenProductNotFound() {
        User manager = buildUser(UserRole.MANAGER);
        DeleteProductCommand command = new DeleteProductCommand(PRODUCT_ID, USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(manager));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found");
    }
}
