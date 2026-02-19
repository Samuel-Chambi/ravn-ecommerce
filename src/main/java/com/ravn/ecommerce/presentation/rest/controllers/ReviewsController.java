package com.ravn.ecommerce.presentation.rest.controllers;

import com.ravn.ecommerce.application.dto.request.review.CreateReviewRequest;
import com.ravn.ecommerce.application.dto.request.review.UpdateReviewRequest;
import com.ravn.ecommerce.application.dto.response.PagedReviewResponse;
import com.ravn.ecommerce.application.dto.response.ReviewResponse;
import com.ravn.ecommerce.application.services.CurrentUserService;
import com.ravn.ecommerce.application.usecases.review.CreateReviewUseCase;
import com.ravn.ecommerce.application.usecases.review.DeleteReviewUseCase;
import com.ravn.ecommerce.application.usecases.review.GetProductReviewsUseCase;
import com.ravn.ecommerce.application.usecases.review.UpdateReviewUseCase;
import com.ravn.ecommerce.application.usecases.review.command.CreateReviewCommand;
import com.ravn.ecommerce.application.usecases.review.command.DeleteReviewCommand;
import com.ravn.ecommerce.application.usecases.review.command.UpdateReviewCommand;
import com.ravn.ecommerce.application.usecases.review.query.ListReviewsQuery;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products/{productId}/reviews")
@RequiredArgsConstructor
public class ReviewsController {
    private final CreateReviewUseCase createReviewUseCase;
    private final UpdateReviewUseCase updateReviewUseCase;
    private final GetProductReviewsUseCase getProductReviewsUseCase;
    private final DeleteReviewUseCase deleteReviewUseCase;
    private final CurrentUserService currentUserService;
    @GetMapping
    public ResponseEntity<PagedReviewResponse> getAllProductReviews(
            @RequestParam(required = false , defaultValue = "") String cursor,
            @RequestParam(required = false , defaultValue = "10") int limit,
            @PathVariable Long productId
    ){
        return ResponseEntity.status(HttpStatus.OK).body(getProductReviewsUseCase.execute(new ListReviewsQuery(cursor, limit, productId)));
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @RequestBody @Valid CreateReviewRequest request,
            @PathVariable Long productId
    ){
        Long userId = currentUserService.getCurrentUserId();
        ReviewResponse response = createReviewUseCase.execute(new CreateReviewCommand(productId , request , userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @RequestBody @Valid UpdateReviewRequest request,
            @PathVariable Long productId,
            @PathVariable Long reviewId
    ){
        Long userId = currentUserService.getCurrentUserId();
        ReviewResponse response = updateReviewUseCase.execute(new UpdateReviewCommand(request, userId , reviewId, productId));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId
    ){
        Long userId = currentUserService.getCurrentUserId();
        deleteReviewUseCase.execute(new DeleteReviewCommand(userId, productId , reviewId));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
