package com.ravn.ecommerce.application.usecases.like;

import com.ravn.ecommerce.application.repositories.LikeRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.like.command.SwitchLikeProductCommand;
import com.ravn.ecommerce.domain.exceptions.ProductNotFound;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.product.Like;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LikeProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LikeProductUseCase useCase;

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 10L;

    private User buildUser() {
        return User.builder()
                .id(USER_ID)
                .email("user@test.com")
                .passwordHash("hashed")
                .role(UserRole.CLIENT)
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
                .build();
    }

    @Test
    @DisplayName("Should like product successfully")
    void shouldLikeProductSuccessfully() {
        SwitchLikeProductCommand command = new SwitchLikeProductCommand(PRODUCT_ID, USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(buildUser()));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(buildProduct()));

        useCase.execute(command);

        verify(likeRepository).save(any(Like.class));
    }

    @Test
    @DisplayName("Should throw when user does not exist")
    void shouldThrowWhenUserNotFound() {
        SwitchLikeProductCommand command = new SwitchLikeProductCommand(PRODUCT_ID, USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFound.class);
    }

    @Test
    @DisplayName("Should throw when product does not exist")
    void shouldThrowWhenProductNotFound() {
        SwitchLikeProductCommand command = new SwitchLikeProductCommand(PRODUCT_ID, USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(buildUser()));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ProductNotFound.class);
    }
}
