package com.ravn.ecommerce.application.useCases.review.command;

public record DeleteReviewCommand(
        Long userId,
        Long productId,
        Long reviewId
){
}
