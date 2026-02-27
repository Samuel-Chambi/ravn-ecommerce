package com.ravn.ecommerce.application.usecases.review;

import com.ravn.ecommerce.application.dto.response.PagedReviewResponse;
import com.ravn.ecommerce.application.dto.response.ReviewResponse;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.ReviewRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.usecases.review.query.ListReviewsQuery;
import com.ravn.ecommerce.application.utils.CursorUtils;
import com.ravn.ecommerce.domain.exceptions.ProductNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetProductReviewsUseCase implements UseCase<ListReviewsQuery, PagedReviewResponse> {
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public PagedReviewResponse execute(ListReviewsQuery query) {
        Long productId = query.productId();
        String cursor = query.cursor();
        int limit = query.limit();

        if (!productRepository.existsById(productId)) {
            throw new ProductNotFound(String.format("Product ID %d does not exist", productId));
        }

        log.info("Listing reviews - cursor: {} , limit: {} , for product ID: {}", cursor, limit, productId);

        ScrollPosition position = query.isFirstPage() ? ScrollPosition.offset() : CursorUtils.decode(cursor);

        Window<ReviewResponse> window = reviewRepository.findAllByProductId(position, limit, productId)
                .map(ReviewResponse::toDto);
        PagedReviewResponse response = PagedReviewResponse.builder()
                .reviews(window.getContent())
                .nextCursor(window.hasNext() ? CursorUtils.encode(window.positionAt(window.size() - 1)) : null)
                .hasNext(window.hasNext())
                .returnedCount(window.getContent().size())
                .build();
        log.info("Returned {} reviews", window.getContent().size());
        return response;
    }

}
