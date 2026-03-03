package com.ravn.ecommerce.application.usecases.review;

import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.ReviewRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.review.command.DeleteReviewCommand;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteReviewUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private DeleteReviewUseCase useCase;

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 10L;
    private static final Long REVIEW_ID = 100L;

    private Review buildReview(Long userId, Long productId) {
        return Review.builder()
                .id(REVIEW_ID)
                .userId(userId)
                .productId(productId)
                .rating(4)
                .comment("Good product")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should delete review successfully")
    void shouldDeleteReviewSuccessfully() {
        DeleteReviewCommand command = new DeleteReviewCommand(USER_ID, PRODUCT_ID, REVIEW_ID);
        Review review = buildReview(USER_ID, PRODUCT_ID);

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(productRepository.existsById(PRODUCT_ID)).thenReturn(true);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));

        useCase.execute(command);

        verify(reviewRepository).deleteById(REVIEW_ID);
    }

    @Test
    @DisplayName("Should throw when user does not exist")
    void shouldThrowWhenUserNotFound() {
        DeleteReviewCommand command = new DeleteReviewCommand(USER_ID, PRODUCT_ID, REVIEW_ID);

        when(userRepository.existsById(USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFound.class);
    }

    @Test
    @DisplayName("Should throw when product does not exist")
    void shouldThrowWhenProductNotFound() {
        DeleteReviewCommand command = new DeleteReviewCommand(USER_ID, PRODUCT_ID, REVIEW_ID);

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(productRepository.existsById(PRODUCT_ID)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ProductNotFound.class);
    }

    @Test
    @DisplayName("Should throw when review does not exist")
    void shouldThrowWhenReviewNotFound() {
        DeleteReviewCommand command = new DeleteReviewCommand(USER_ID, PRODUCT_ID, REVIEW_ID);

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(productRepository.existsById(PRODUCT_ID)).thenReturn(true);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw when user tries to delete another user's review")
    void shouldThrowWhenDeletingOtherUsersReview() {
        DeleteReviewCommand command = new DeleteReviewCommand(USER_ID, PRODUCT_ID, REVIEW_ID);
        Review review = buildReview(999L, PRODUCT_ID); // different user

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(productRepository.existsById(PRODUCT_ID)).thenReturn(true);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(BusinessRuleViolation.class)
                .hasMessageContaining("cannot delete other user's reviews");
    }

    @Test
    @DisplayName("Should throw when review does not belong to product")
    void shouldThrowWhenReviewDoesNotBelongToProduct() {
        DeleteReviewCommand command = new DeleteReviewCommand(USER_ID, PRODUCT_ID, REVIEW_ID);
        Review review = buildReview(USER_ID, 999L); // different product

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(productRepository.existsById(PRODUCT_ID)).thenReturn(true);
        when(reviewRepository.findById(REVIEW_ID)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(BusinessRuleViolation.class)
                .hasMessageContaining("does not belong to product");
    }
}
