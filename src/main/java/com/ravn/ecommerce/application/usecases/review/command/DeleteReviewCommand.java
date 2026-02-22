package com.ravn.ecommerce.application.usecases.review.command;

public record DeleteReviewCommand(
        Long userId,
        Long productId,
        Long reviewId
){
}
