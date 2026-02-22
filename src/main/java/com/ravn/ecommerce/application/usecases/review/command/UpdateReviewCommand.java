package com.ravn.ecommerce.application.usecases.review.command;

import com.ravn.ecommerce.application.dto.request.review.UpdateReviewRequest;

public record UpdateReviewCommand(
        UpdateReviewRequest request,
        Long userId,
        Long reviewId,
        Long productId

) {
}
