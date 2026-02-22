package com.ravn.ecommerce.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedReviewResponse {
    private List<ReviewResponse> reviews;
    private String nextCursor;
    private boolean hasNext;
    private int returnedCount;
}
