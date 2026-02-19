package com.ravn.ecommerce.application.usecases.review;

import com.ravn.ecommerce.application.dto.request.review.UpdateReviewRequest;
import com.ravn.ecommerce.application.dto.response.ReviewResponse;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.ReviewRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.usecases.review.command.UpdateReviewCommand;
import com.ravn.ecommerce.domain.exceptions.BusinessRuleViolation;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.ProductNotFound;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.product.Review;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateReviewUseCase implements UseCase<UpdateReviewCommand, ReviewResponse> {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ReviewResponse execute(UpdateReviewCommand command) {
        Long userId = command.userId();
        Long productId = command.productId();
        Long reviewId = command.reviewId();
        UpdateReviewRequest request = command.request();
        log.info("Updating review for product ID {} by user ID {}" , productId , userId);
        if(!userRepository.existsById(userId)) {
            throw new UserNotFound(String.format("User ID %d does not exist" , userId));
        }
        if(!productRepository.existsById(productId)) {
            throw new ProductNotFound(String.format("Product ID %d does not exist" , productId));
        }
        Review currentReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Review ID %d does not exist" , reviewId)));

        if(!Objects.equals(currentReview.getUserId(), userId)){
            throw new BusinessRuleViolation("User cannot update other user's reviews");
        }

        if(!Objects.equals(currentReview.getProductId() , productId)){
            throw new BusinessRuleViolation(String.format("Review ID %d does not belong to product ID %d" , reviewId , productId));
        }

        if(request.getComment() != null){
            currentReview.setComment(request.getComment());
        }

        if(request.getRating() > 0){
            currentReview.updateRating(request.getRating());
        }
        currentReview.setUpdatedAt(LocalDateTime.now());
        Review updatedReview = reviewRepository.save(currentReview);
        log.info("Review ID {} updated successfully" , reviewId);

        return ReviewResponse.toDto(updatedReview);
    }
}
