package com.ravn.ecommerce.application.useCases.review.command;

import com.ravn.ecommerce.application.dto.request.review.CreateReviewRequest;

public record CreateReviewCommand(
        Long productId,
        CreateReviewRequest request,
        Long userId
) {
}
