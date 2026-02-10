package com.ravn.ecommerce.domain.model.product;

import com.ravn.ecommerce.domain.exceptions.BussinessRoleViolation;

import java.time.LocalDateTime;
import java.util.Objects;

public class Review {
    private Long id;
    private Long userId;
    private Long productId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public Review() {
    }

    public Review(Long id, Long userId, Long productId, int rating, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public void updateRating(int newRating) {
        if (newRating < 1 || newRating > 5) {
            throw new BussinessRoleViolation("Rating must be between 1 and 5");
        }
        this.rating = newRating;
    }

    public void updateComment(String newComment) {
        this.comment = newComment;
    }

    public boolean isPositive() {
        return rating >= 4;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
