package com.ravn.ecommerce.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for cursor-based paginated product lists.
 * Contains the products and cursor for next page.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedProductResponse {
    private List<ProductResponse> products;
    private String nextCursor; // Cursor for next page (null if no more pages)
    private boolean hasNext; // Whether there are more results
    private int returnedCount; // Number of items in this response
}
