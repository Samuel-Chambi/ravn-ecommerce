package com.ravn.ecommerce.application.useCases.review;

import com.ravn.ecommerce.application.dto.request.review.CreateReviewRequest;
import com.ravn.ecommerce.application.dto.response.ReviewResponse;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.ReviewRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.useCases.UseCase;
import com.ravn.ecommerce.application.useCases.review.command.CreateReviewCommand;
import com.ravn.ecommerce.domain.exceptions.ProductNotFound;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.product.Review;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreateReviewUseCase implements UseCase <CreateReviewCommand, ReviewResponse> {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    @Override
    @Transactional
    public ReviewResponse execute(CreateReviewCommand command){
        log.info("Creating review for product ID: {}" , command.productId());
        Long productId = command.productId();
        Long userId = command.userId();
        if (!userRepository.existsById(userId)) {
            throw new UserNotFound(String.format("User ID %d does not exist" , userId));
        }
        if(!productRepository.existsById(productId)) {
            throw new ProductNotFound(String.format("Product ID %d does not exist" , productId));
        }

        CreateReviewRequest request = command.request();
        Review review = Review.builder()
                .userId(userId)
                .productId(productId)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Review for product ID: {} created successfully" , productId);
        return ReviewResponse.toDto(saved);
    }
}
