package com.ravn.ecommerce.application.useCases.review;

import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.ReviewRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.useCases.UseCase;
import com.ravn.ecommerce.application.useCases.review.command.DeleteReviewCommand;
import com.ravn.ecommerce.domain.exceptions.BusinessRuleViolation;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.ProductNotFound;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.product.Review;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeleteReviewUseCase implements UseCase<DeleteReviewCommand, Void> {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public Void execute(DeleteReviewCommand command){
        Long userId = command.userId();
        Long productId = command.productId();
        Long reviewId = command.reviewId();

        log.info("Deleting review for product ID {} by user ID {}" , productId , userId);

        if(!userRepository.existsById(userId)) {
            throw new UserNotFound(String.format("User ID %d does not exist" , userId));
        }
        if(!productRepository.existsById(productId)) {
            throw new ProductNotFound(String.format("Product ID %d does not exist" , productId));
        }
        Review currentReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Review ID %d does not exist" , reviewId)));

        if(!Objects.equals(currentReview.getUserId(), userId)){
            throw new BusinessRuleViolation("User cannot delete other user's reviews");
        }

        if(!Objects.equals(currentReview.getProductId() , productId)){
            throw new BusinessRuleViolation(String.format("Review ID %d does not belong to product ID %d" , reviewId , productId));
        }

        reviewRepository.deleteById(reviewId);
        log.info("Review ID {} deleted successfully" , reviewId);
        return null;
    }
}
