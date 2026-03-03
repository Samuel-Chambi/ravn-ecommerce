package com.ravn.ecommerce.application.usecases.review;

import com.ravn.ecommerce.application.dto.request.review.UpdateReviewRequest;
import com.ravn.ecommerce.application.dto.response.ReviewResponse;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.ReviewRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.review.command.UpdateReviewCommand;
import com.ravn.ecommerce.domain.exceptions.BusinessRuleViolation;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateReviewUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private UpdateReviewUseCase useCase;

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 10L;
    private static final Long REVIEW_ID = 100L;

    private Review buildReview(Long userId, Long productId) {
        return Review.builder()
                .id(REVIEW_ID)
                .userId(userId)
                .productId(productId)
                .rating(3)
                .comment("Decent product")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should update review comment and rating successfully")
    void shouldUpdateReviewSuccessfully() {
        Review review = buildReview(USER_ID, PRODUCT_ID);
        UpdateReviewRequest request = new UpdateReviewRequest(5, "Updated comment with more detail");
        UpdateReviewCommand command = new UpdateReviewCommand(request, USER_ID, REVIEW_ID, PRODUCT_ID);

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(productRepository.existsById(PRODUCT_ID)).thenReturn(true);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponse response = useCase.execute(command);

        assertThat(response).isNotNull();
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getComment()).isEqualTo("Updated comment with more detail");
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("Should update only comment when rating is 0")
    void shouldUpdateOnlyComment() {
        Review review = buildReview(USER_ID, PRODUCT_ID);
        UpdateReviewRequest request = new UpdateReviewRequest(0, "Only updating comment here");
        UpdateReviewCommand command = new UpdateReviewCommand(request, USER_ID, REVIEW_ID, PRODUCT_ID);

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(productRepository.existsById(PRODUCT_ID)).thenReturn(true);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponse response = useCase.execute(command);

        assertThat(response).isNotNull();
        assertThat(response.getRating()).isEqualTo(3); // unchanged
        assertThat(response.getComment()).isEqualTo("Only updating comment here");
    }

    @Test
    @DisplayName("Should throw when user does not exist")
    void shouldThrowWhenUserNotFound() {
        UpdateReviewRequest request = new UpdateReviewRequest(4, "Updated");
        UpdateReviewCommand command = new UpdateReviewCommand(request, USER_ID, REVIEW_ID, PRODUCT_ID);

        when(userRepository.existsById(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFound.class);
    }

    @Test
    @DisplayName("Should throw when product does not exist")
    void shouldThrowWhenProductNotFound() {
        UpdateReviewRequest request = new UpdateReviewRequest(4, "Updated");
        UpdateReviewCommand command = new UpdateReviewCommand(request, USER_ID, REVIEW_ID, PRODUCT_ID);

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(productRepository.existsById(PRODUCT_ID)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ProductNotFound.class);
    }

    @Test
    @DisplayName("Should throw when review does not exist")
    void shouldThrowWhenReviewNotFound() {
        UpdateReviewRequest request = new UpdateReviewRequest(4, "Updated");
        UpdateReviewCommand command = new UpdateReviewCommand(request, USER_ID, REVIEW_ID, PRODUCT_ID);

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(productRepository.existsById(PRODUCT_ID)).thenReturn(true);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw when user tries to update another user's review")
    void shouldThrowWhenUpdatingOtherUsersReview() {
        Review review = buildReview(999L, PRODUCT_ID);
        UpdateReviewRequest request = new UpdateReviewRequest(4, "Updated");
        UpdateReviewCommand command = new UpdateReviewCommand(request, USER_ID, REVIEW_ID, PRODUCT_ID);

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(productRepository.existsById(PRODUCT_ID)).thenReturn(true);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(BusinessRuleViolation.class)
                .hasMessageContaining("cannot update other user's reviews");
    }

    @Test
    @DisplayName("Should throw when review does not belong to product")
    void shouldThrowWhenReviewDoesNotBelongToProduct() {
        Review review = buildReview(USER_ID, 999L);
        UpdateReviewRequest request = new UpdateReviewRequest(4, "Updated");
        UpdateReviewCommand command = new UpdateReviewCommand(request, USER_ID, REVIEW_ID, PRODUCT_ID);

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(productRepository.existsById(PRODUCT_ID)).thenReturn(true);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(BusinessRuleViolation.class)
                .hasMessageContaining("does not belong to product");
    }
}
