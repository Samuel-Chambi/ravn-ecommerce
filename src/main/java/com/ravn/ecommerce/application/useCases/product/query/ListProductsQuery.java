package com.ravn.ecommerce.application.useCases.product.query;

import org.springframework.data.domain.Sort;

/**
 * Query object for listing products with cursor-based pagination and optional
 * category filter.
 * Uses cursor (keyset) pagination for better performance and consistency.
 */
public record ListProductsQuery(
        String cursor, // Cursor for pagination (null for first page)
        int limit, // Number of items to fetch (1-100)
        String sortBy, // Field to sort by (default: "id")
        String sortDirection, // "asc" or "desc" (default: "asc")
        Long categoryId, // Optional - null means all products
        String search, // Optional - search by name
        boolean showDisabled // false = only enabled products, true = all products
) {
    // Compact constructor with validations
    public ListProductsQuery {
        if (limit <= 0 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100");
        }
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "id"; // Default sort by ID for cursor pagination
        }
        if (sortDirection == null || sortDirection.isBlank()) {
            sortDirection = "asc"; // Default direction
        }
    }

    /**
     * Helper method to get Sort.Direction from string
     */
    public Sort.Direction getSortDirection() {
        return "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
    }

    /**
     * Check if filtering by category
     */
    public boolean hasCategory() {
        return categoryId != null;
    }

    /**
     * Check if filtering by search term
     */
    public boolean hasSearch() {
        return search != null && !search.isBlank();
    }

    /**
     * Check if this is the first page (no cursor)
     */
    public boolean isFirstPage() {
        return cursor == null || cursor.isBlank();
    }
}
