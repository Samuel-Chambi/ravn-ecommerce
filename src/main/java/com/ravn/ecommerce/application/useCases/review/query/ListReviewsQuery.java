package com.ravn.ecommerce.application.useCases.review.query;

public record ListReviewsQuery(
        String cursor,
        int limit,
        Long productId
) {
    public ListReviewsQuery {
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Limit value must be between 1 and 100");
        }
    }

    public boolean isFirstPage () { return cursor == null || cursor.isBlank(); }
}
