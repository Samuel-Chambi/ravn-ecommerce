package com.ravn.ecommerce.presentation.rest.controllers;

import com.ravn.ecommerce.application.dto.response.PagedProductResponse;
import com.ravn.ecommerce.application.services.CurrentUserService;
import com.ravn.ecommerce.application.useCases.like.DislikeProductUseCase;
import com.ravn.ecommerce.application.useCases.like.GetLikedProductsUseCase;
import com.ravn.ecommerce.application.useCases.like.LikeProductUseCase;
import com.ravn.ecommerce.application.useCases.like.command.SwitchLikeProductCommand;
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
    public ResponseEntity<PagedProductResponse> getLikedProducts() {
        Long userId = currentUserService.getCurrentUserId();
        PagedProductResponse response = getLikedProductsUseCase.execute(userId);
        return ResponseEntity.ok(response);
    }
}
