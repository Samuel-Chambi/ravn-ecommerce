package com.ravn.ecommerce.application.usecases.like;

import com.ravn.ecommerce.application.dto.response.ProductResponse;
import com.ravn.ecommerce.application.repositories.LikeRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.like.command.GetLikedProductsCommand;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.product.Like;
import com.ravn.ecommerce.domain.model.product.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetLikedProductsUseCaseTest {

    @Mock
    private LikeRepository likeRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetLikedProductsUseCase useCase;

    private static final Long USER_ID = 1L;

    @Test
    @DisplayName("Should return liked products for user")
    void shouldReturnLikedProducts() {
        GetLikedProductsCommand command = new GetLikedProductsCommand(USER_ID, ScrollPosition.offset(), Limit.of(10));
        Like like = Like.builder().id(1L).userId(USER_ID).productId(10L).createdAt(LocalDateTime.now()).build();
        Window<Like> likesWindow = Window.from(List.of(like), ScrollPosition::offset);
        Product product = Product.builder()
                .id(10L).name("Liked Product").price(new BigDecimal("25.00"))
                .isEnabled(true).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(likeRepository.findAllByUserId(eq(USER_ID), any(ScrollPosition.class), any(Limit.class)))
                .thenReturn(likesWindow);
        when(productRepository.findAllById(List.of(10L))).thenReturn(List.of(product));

        Window<ProductResponse> result = useCase.execute(command);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Liked Product");
    }

    @Test
    @DisplayName("Should return empty window when no liked products")
    void shouldReturnEmptyWhenNoLikes() {
        GetLikedProductsCommand command = new GetLikedProductsCommand(USER_ID, ScrollPosition.offset(), Limit.of(10));
        Window<Like> emptyWindow = Window.from(List.of(), ScrollPosition::offset);

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(likeRepository.findAllByUserId(eq(USER_ID), any(ScrollPosition.class), any(Limit.class)))
                .thenReturn(emptyWindow);

        Window<ProductResponse> result = useCase.execute(command);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Should throw when user does not exist")
    void shouldThrowWhenUserNotFound() {
        GetLikedProductsCommand command = new GetLikedProductsCommand(USER_ID, ScrollPosition.offset(), Limit.of(10));

        when(userRepository.existsById(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFound.class);
    }
}
