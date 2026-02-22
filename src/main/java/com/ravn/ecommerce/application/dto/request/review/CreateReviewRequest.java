package com.ravn.ecommerce.application.dto.request.review;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateReviewRequest {
    @NotNull(message = "Review rating is required")
    @Min(value = 1 , message = "Rating must be at least 1")
    @Max(value = 5 , message = "Rating must be at most 5")
    private int rating;
    @NotBlank(message = "Review comment is required")
    @Size(min = 10, max = 500, message = "Review must be between 10 and 500 characters")
    private String comment;
}
