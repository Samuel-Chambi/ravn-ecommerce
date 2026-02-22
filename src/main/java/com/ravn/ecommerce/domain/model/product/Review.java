package com.ravn.ecommerce.domain.model.product;

import com.ravn.ecommerce.domain.exceptions.BusinessRuleViolation;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Review {
    private Long id;
    private Long userId;
    private Long productId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void updateRating(int newRating) {
        if (newRating < 1 || newRating > 5) {
            throw new BusinessRuleViolation("Rating must be between 1 and 5");
        }
        this.rating = newRating;
    }

    public boolean isPositive() {
        return rating >= 4;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return Objects.equals(id, review.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
