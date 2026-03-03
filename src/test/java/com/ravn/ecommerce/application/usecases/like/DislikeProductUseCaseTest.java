package com.ravn.ecommerce.application.usecases.like;

import com.ravn.ecommerce.application.repositories.LikeRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.like.command.SwitchLikeProductCommand;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DislikeProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private DislikeProductUseCase useCase;

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 10L;
    private static final Long LIKE_ID = 100L;

    @Test
    @DisplayName("Should dislike product successfully")
    void shouldDislikeProductSuccessfully() {
        SwitchLikeProductCommand command = new SwitchLikeProductCommand(PRODUCT_ID, USER_ID);
        User user = User.builder().id(USER_ID).email("user@test.com").role(UserRole.CLIENT).createdAt(LocalDateTime.now()).build();
        Product product = Product.builder().id(PRODUCT_ID).name("Test").price(new BigDecimal("25.00")).isEnabled(true).build();
        Like like = Like.builder().id(LIKE_ID).userId(USER_ID).productId(PRODUCT_ID).createdAt(LocalDateTime.now()).build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(likeRepository.findByUserIdAndProductId(USER_ID, PRODUCT_ID)).thenReturn(Optional.of(like));

        useCase.execute(command);

        verify(likeRepository).deleteById(LIKE_ID);
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
        User user = User.builder().id(USER_ID).email("user@test.com").role(UserRole.CLIENT).createdAt(LocalDateTime.now()).build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ProductNotFound.class);
    }

    @Test
    @DisplayName("Should throw when like does not exist")
    void shouldThrowWhenLikeNotFound() {
        SwitchLikeProductCommand command = new SwitchLikeProductCommand(PRODUCT_ID, USER_ID);
        User user = User.builder().id(USER_ID).email("user@test.com").role(UserRole.CLIENT).createdAt(LocalDateTime.now()).build();
        Product product = Product.builder().id(PRODUCT_ID).name("Test").price(new BigDecimal("25.00")).isEnabled(true).build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(likeRepository.findByUserIdAndProductId(USER_ID, PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
