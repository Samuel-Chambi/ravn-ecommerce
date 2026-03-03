package com.ravn.ecommerce.application.usecases.product;

import com.ravn.ecommerce.application.dto.response.ProductResponse;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.product.command.SwitchEnabledCommand;
import com.ravn.ecommerce.domain.exceptions.ProductNotFound;
import com.ravn.ecommerce.domain.exceptions.UnauthorizedException;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnableProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EnableProductUseCase useCase;

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

    private Product buildDisabledProduct() {
        return Product.builder()
                .id(PRODUCT_ID)
                .name("Test Product")
                .price(new BigDecimal("25.00"))
                .isEnabled(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should enable product successfully when user is MANAGER")
    void shouldEnableProductSuccessfully() {
        User manager = buildUser(UserRole.MANAGER);
        Product product = buildDisabledProduct();
        SwitchEnabledCommand command = new SwitchEnabledCommand(USER_ID, PRODUCT_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(manager));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = useCase.execute(command);

        assertThat(response).isNotNull();
        assertThat(response.getIsEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should throw when user does not exist")
    void shouldThrowWhenUserNotFound() {
        SwitchEnabledCommand command = new SwitchEnabledCommand(USER_ID, PRODUCT_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFound.class);
    }

    @Test
    @DisplayName("Should throw when user is not MANAGER")
    void shouldThrowWhenUserNotManager() {
        User client = buildUser(UserRole.CLIENT);
        SwitchEnabledCommand command = new SwitchEnabledCommand(USER_ID, PRODUCT_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("MANAGER");
    }

    @Test
    @DisplayName("Should throw when product does not exist")
    void shouldThrowWhenProductNotFound() {
        User manager = buildUser(UserRole.MANAGER);
        SwitchEnabledCommand command = new SwitchEnabledCommand(USER_ID, PRODUCT_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(manager));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ProductNotFound.class);
    }
}
