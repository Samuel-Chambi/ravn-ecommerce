package com.ravn.ecommerce.application.usecases.like.command;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;

public record GetLikedProductsCommand(
        Long userId,
        ScrollPosition position,
        Limit limit) {
}
