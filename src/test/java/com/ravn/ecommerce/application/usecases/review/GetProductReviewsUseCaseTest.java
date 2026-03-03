package com.ravn.ecommerce.application.usecases.review;

import com.ravn.ecommerce.application.dto.response.PagedReviewResponse;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.ReviewRepository;
import com.ravn.ecommerce.application.usecases.review.query.ListReviewsQuery;
import com.ravn.ecommerce.domain.exceptions.ProductNotFound;
import com.ravn.ecommerce.domain.model.product.Review;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProductReviewsUseCaseTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private GetProductReviewsUseCase useCase;

    private static final Long PRODUCT_ID = 10L;

    private Review buildReview(Long id) {
        return Review.builder()
                .id(id)
                .userId(1L)
                .productId(PRODUCT_ID)
                .rating(4)
                .comment("Good product review")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should return reviews for product")
    void shouldReturnProductReviews() {
        ListReviewsQuery query = new ListReviewsQuery(null, 10, PRODUCT_ID);
        List<Review> reviews = List.of(buildReview(1L), buildReview(2L));
        Window<Review> window = Window.from(reviews, ScrollPosition::offset);

        when(productRepository.existsById(PRODUCT_ID)).thenReturn(true);
        when(reviewRepository.findAllByProductId(any(ScrollPosition.class), eq(10), eq(PRODUCT_ID)))
                .thenReturn(window);

        PagedReviewResponse response = useCase.execute(query);

        assertThat(response).isNotNull();
        assertThat(response.getReviews()).hasSize(2);
        assertThat(response.getReturnedCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should throw when product does not exist")
    void shouldThrowWhenProductNotFound() {
        ListReviewsQuery query = new ListReviewsQuery(null, 10, PRODUCT_ID);

        when(productRepository.existsById(PRODUCT_ID)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(query))
                .isInstanceOf(ProductNotFound.class);
    }

    @Test
    @DisplayName("Should return empty result when no reviews exist")
    void shouldReturnEmptyWhenNoReviews() {
        ListReviewsQuery query = new ListReviewsQuery(null, 10, PRODUCT_ID);
        Window<Review> emptyWindow = Window.from(List.of(), ScrollPosition::offset);

        when(productRepository.existsById(PRODUCT_ID)).thenReturn(true);
        when(reviewRepository.findAllByProductId(any(ScrollPosition.class), eq(10), eq(PRODUCT_ID)))
                .thenReturn(emptyWindow);

        PagedReviewResponse response = useCase.execute(query);

        assertThat(response).isNotNull();
        assertThat(response.getReviews()).isEmpty();
        assertThat(response.isHasNext()).isFalse();
    }
}
