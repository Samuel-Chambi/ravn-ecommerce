package com.ravn.ecommerce.application.usecases.review;

import com.ravn.ecommerce.application.dto.request.review.CreateReviewRequest;
import com.ravn.ecommerce.application.dto.response.ReviewResponse;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.ReviewRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.review.command.CreateReviewCommand;
import com.ravn.ecommerce.domain.exceptions.ProductNotFound;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.product.Review;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateReviewUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private CreateReviewUseCase useCase;

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 10L;

    private CreateReviewRequest buildRequest() {
        return CreateReviewRequest.builder()
                .rating(5)
                .comment("Excellent product, highly recommended!")
                .build();
    }

    private Review buildSavedReview() {
        return Review.builder()
                .id(1L)
                .userId(USER_ID)
                .productId(PRODUCT_ID)
                .rating(5)
                .comment("Excellent product, highly recommended!")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create review successfully")
    void shouldCreateReviewSuccessfully() {
        CreateReviewRequest request = buildRequest();
        CreateReviewCommand command = new CreateReviewCommand(PRODUCT_ID, request, USER_ID);
        Review savedReview = buildSavedReview();

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(productRepository.existsById(PRODUCT_ID)).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        ReviewResponse response = useCase.execute(command);

        assertThat(response).isNotNull();
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(response.getUserId()).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("Should throw when user does not exist")
    void shouldThrowWhenUserNotFound() {
        CreateReviewCommand command = new CreateReviewCommand(PRODUCT_ID, buildRequest(), USER_ID);

        when(userRepository.existsById(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFound.class);
    }

    @Test
    @DisplayName("Should throw when product does not exist")
    void shouldThrowWhenProductNotFound() {
        CreateReviewCommand command = new CreateReviewCommand(PRODUCT_ID, buildRequest(), USER_ID);

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(productRepository.existsById(PRODUCT_ID)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ProductNotFound.class);
    }
}
