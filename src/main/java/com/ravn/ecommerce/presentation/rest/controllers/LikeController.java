package com.ravn.ecommerce.presentation.rest.controllers;

import com.ravn.ecommerce.application.dto.response.PagedProductResponse;
import com.ravn.ecommerce.application.services.CurrentUserService;
import com.ravn.ecommerce.application.usecases.like.DislikeProductUseCase;
import com.ravn.ecommerce.application.usecases.like.GetLikedProductsUseCase;
import com.ravn.ecommerce.application.usecases.like.LikeProductUseCase;
import com.ravn.ecommerce.application.usecases.like.command.SwitchLikeProductCommand;
import com.ravn.ecommerce.application.usecases.like.command.GetLikedProductsCommand;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Window;
import com.ravn.ecommerce.application.dto.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class LikeController {

    private final LikeProductUseCase likeProductUseCase;
    private final DislikeProductUseCase dislikeProductUseCase;
    private final GetLikedProductsUseCase getLikedProductsUseCase;
    private final CurrentUserService currentUserService;

    @PostMapping("/{productId}")
    public ResponseEntity<Void> likeProduct(@PathVariable Long productId) {
        Long userId = currentUserService.getCurrentUserId();
        likeProductUseCase.execute(new SwitchLikeProductCommand(productId, userId));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> dislikeProduct(@PathVariable Long productId) {
        Long userId = currentUserService.getCurrentUserId();
        dislikeProductUseCase.execute(new SwitchLikeProductCommand(productId, userId));
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Window<ProductResponse>> getLikedProducts(
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = currentUserService.getCurrentUserId();
        Window<ProductResponse> response = getLikedProductsUseCase.execute(
                new GetLikedProductsCommand(userId, ScrollPosition.keyset(), Limit.of(limit)));
        return ResponseEntity.ok(response);
    }
}
