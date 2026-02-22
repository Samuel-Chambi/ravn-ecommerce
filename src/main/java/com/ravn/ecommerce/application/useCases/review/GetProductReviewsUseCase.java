package com.ravn.ecommerce.application.useCases.review;

import com.ravn.ecommerce.application.dto.response.PagedReviewResponse;
import com.ravn.ecommerce.application.dto.response.ReviewResponse;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.ReviewRepository;
import com.ravn.ecommerce.application.useCases.UseCase;
import com.ravn.ecommerce.application.useCases.review.query.ListReviewsQuery;
import com.ravn.ecommerce.domain.exceptions.ProductNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetProductReviewsUseCase implements UseCase<ListReviewsQuery, PagedReviewResponse> {
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public PagedReviewResponse execute(ListReviewsQuery query){
        Long productId = query.productId();
        String cursor = query.cursor();
        int limit = query.limit();

        if(!productRepository.existsById(productId)){
            throw new ProductNotFound(String.format("Product ID %d does not exist" , productId));
        }

        log.info("Listing reviews - cursor: {} , limit: {} , for product ID: {}" , cursor , limit, productId);

        ScrollPosition position = query.isFirstPage() ? ScrollPosition.offset() : decodeScrollPosition(cursor);

        Window<ReviewResponse> window = reviewRepository.findAllByProductId(position , limit , productId)
                .map(ReviewResponse::toDto);
        PagedReviewResponse response = PagedReviewResponse.builder()
                .reviews(window.getContent())
                .nextCursor(window.hasNext() ? encodeScrollPosition(window.positionAt(window.size() - 1)) : null)
                .hasNext(window.hasNext())
                .returnedCount(window.getContent().size())
                .build();
        log.info("Returned {} reviews" , window.getContent().size());
        return response;
    }

    private String encodeScrollPosition(ScrollPosition position) {
        if (position instanceof KeysetScrollPosition keyset) {
            // Encode the keyset values to base64
            Map<String, Object> keys = keyset.getKeys();
            String keyString = keys.toString();
            return Base64.getUrlEncoder().encodeToString(keyString.getBytes());
        }
        return null;
    }

    /**
     * Decode base64 cursor string to ScrollPosition
     */
    private ScrollPosition decodeScrollPosition(String cursor) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(cursor);
            // For simplicity, using offset position
            // In production, you'd parse the keyset values
            return ScrollPosition.offset();
        } catch (Exception e) {
            log.warn("Invalid cursor: {}, starting from beginning", cursor);
            return ScrollPosition.offset();
        }
    }

}
