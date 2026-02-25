package com.ravn.ecommerce.presentation.graphql;

import com.ravn.ecommerce.application.dto.response.ProductResponse;
import com.ravn.ecommerce.application.usecases.like.GetLikedProductsUseCase;
import com.ravn.ecommerce.application.usecases.like.LikeProductUseCase;
import com.ravn.ecommerce.application.usecases.like.DislikeProductUseCase;
import com.ravn.ecommerce.application.usecases.like.command.GetLikedProductsCommand;
import com.ravn.ecommerce.application.usecases.like.command.SwitchLikeProductCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LikeResolver {
    private final GetLikedProductsUseCase getLikedProductsUseCase;
    private final LikeProductUseCase likeProductUseCase;
    private final DislikeProductUseCase dislikeProductUseCase;

    // ── Query ─
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Window<ProductResponse> likedProducts(ScrollSubrange subrange) {
        Long userId = extractUserId();
        ScrollPosition scrollPosition = subrange.position().orElse(ScrollPosition.keyset());
        Limit limit = Limit.of(subrange.count().orElse(10));
        return getLikedProductsUseCase.execute(
                new GetLikedProductsCommand(userId, scrollPosition, limit));
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean likeProduct(@Argument Long productId) {
        Long userId = extractUserId();
        log.info("GraphQL: user {} liking product {}", userId, productId);
        likeProductUseCase.execute(new SwitchLikeProductCommand(productId, userId));
        return true;
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean unlikeProduct(@Argument Long productId) {
        Long userId = extractUserId();
        log.info("GraphQL: user {} unliking product {}", userId, productId);
        dislikeProductUseCase.execute(new SwitchLikeProductCommand(productId, userId));
        return true;
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
