package com.ravn.ecommerce.application.useCases.category.query;

public record ListCategoriesQuery(
        String cursor,
        int limit
) {
    public ListCategoriesQuery {
        if (limit <= 0 || limit > 100) {
            throw new IllegalArgumentException("Limit value must be between 1 and 100");
        }
    }

    /**
     * Check if this is the first page (no cursor)
     */
    public boolean isFirstPage() {
        return cursor == null || cursor.isBlank();
    }
}
