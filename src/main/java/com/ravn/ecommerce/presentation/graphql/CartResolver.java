package com.ravn.ecommerce.presentation.graphql;

import com.ravn.ecommerce.application.dto.response.CartResponse;
import com.ravn.ecommerce.application.usecases.cart.*;
import com.ravn.ecommerce.application.usecases.cart.command.AddItemToCartCommand;
import com.ravn.ecommerce.application.usecases.cart.command.RemoveItemFromCartCommand;
import com.ravn.ecommerce.application.usecases.cart.command.UpdateItemQuantityCommand;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.dto.response.CartItemResponse;
import com.ravn.ecommerce.domain.model.product.Product;
import com.ravn.ecommerce.domain.model.product.ProductImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CartResolver {

    private final GetCurrentUserCartUseCase getCurrentUserCartUseCase;
    private final AddItemToCartUseCase addItemToCartUseCase;
    private final UpdateItemQuantityUseCase updateItemQuantityUseCase;
    private final RemoveItemFromCartUseCase removeItemFromCartUseCase;
    private final ClearCartUseCase clearCartUseCase;
    private final ProductRepository productRepository;

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

    // ── Field Resolvers ──────────────────────────────────────────────────────

    @BatchMapping(typeName = "CartItem", field = "primaryImage")
    public Map<CartItemResponse, String> primaryImages(List<CartItemResponse> cartItems) {
        List<Long> productIds = cartItems.stream()
                .map(CartItemResponse::getProductId)
                .distinct()
                .toList();

        Map<Long, String> imageMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(
                        Product::getId,
                        product -> {
                            ProductImage img = product.getPrimaryImage();
                            return img != null ? img.getImageUrl() : "";
                        }));

        return cartItems.stream()
                .collect(Collectors.toMap(
                        item -> item,
                        item -> imageMap.getOrDefault(item.getProductId(), "")));
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
