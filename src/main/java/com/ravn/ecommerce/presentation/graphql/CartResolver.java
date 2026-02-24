package com.ravn.ecommerce.presentation.graphql;

import com.ravn.ecommerce.application.dto.response.CartResponse;
import com.ravn.ecommerce.application.usecases.cart.*;
import com.ravn.ecommerce.application.usecases.cart.command.AddItemToCartCommand;
import com.ravn.ecommerce.application.usecases.cart.command.RemoveItemFromCartCommand;
import com.ravn.ecommerce.application.usecases.cart.command.UpdateItemQuantityCommand;
import com.ravn.ecommerce.application.usecases.product.GetProductByIdUseCase;
import com.ravn.ecommerce.application.dto.response.CartItemResponse;
import com.ravn.ecommerce.application.dto.response.ProductResponse;
import com.ravn.ecommerce.domain.model.product.ProductImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CartResolver {

    private final GetCurrentUserCartUseCase getCurrentUserCartUseCase;
    private final AddItemToCartUseCase addItemToCartUseCase;
    private final UpdateItemQuantityUseCase updateItemQuantityUseCase;
    private final RemoveItemFromCartUseCase removeItemFromCartUseCase;
    private final ClearCartUseCase clearCartUseCase;
    private final GetProductByIdUseCase getProductByIdUseCase;

    // ── Queries ───────────────────────────────────────────────────────────────

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public CartResponse myCart() {
        Long userId = extractUserId();
        log.info("GraphQL: fetching cart for user {}", userId);
        return getCurrentUserCartUseCase.execute(userId);
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public CartResponse addToCart(@Argument Map<String, Object> input) {
        Long userId = extractUserId();
        Long productId = Long.parseLong(input.get("productId").toString());
        Integer quantity = (Integer) input.get("quantity");
        return addItemToCartUseCase.execute(new AddItemToCartCommand(userId, productId, quantity));
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public CartResponse updateCartItem(@Argument Map<String, Object> input) {
        Long userId = extractUserId();
        Long productId = Long.parseLong(input.get("cartItemId").toString());
        Integer quantity = (Integer) input.get("quantity");
        return updateItemQuantityUseCase.execute(new UpdateItemQuantityCommand(userId, productId, quantity));
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public CartResponse removeFromCart(@Argument Long cartItemId) {
        Long userId = extractUserId();
        return removeItemFromCartUseCase.execute(new RemoveItemFromCartCommand(userId, cartItemId));
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean clearCart() {
        Long userId = extractUserId();
        clearCartUseCase.execute(userId);
        return true;
    }

    // ── Fields ────────────────────────────────────────────────────────────────

    @SchemaMapping(typeName = "CartItem", field = "primaryImage")
    public String getPrimaryImage(CartItemResponse cartItem) {
        try {
            ProductResponse product = getProductByIdUseCase.execute(cartItem.getProductId());
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                return product.getImages().stream()
                        .filter(ProductImage::isPrimary)
                        .findFirst()
                        .map(ProductImage::getImageUrl)
                        .orElse(product.getImages().getFirst().getImageUrl());
            }
        } catch (Exception e) {
            log.warn("Could not fetch primary image for product ID: {}", cartItem.getProductId());
        }
        return null; // Return null intentionally if not found, to indicate no image
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Long extractUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new IllegalStateException("User is not authenticated");
        }
        return Long.parseLong(auth.getName());
    }
}
