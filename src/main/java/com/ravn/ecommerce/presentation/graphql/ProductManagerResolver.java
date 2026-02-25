package com.ravn.ecommerce.presentation.graphql;

import com.ravn.ecommerce.application.dto.response.ProductResponse;
import com.ravn.ecommerce.application.usecases.product.DeleteProductUseCase;
import com.ravn.ecommerce.application.usecases.product.DisableProductUseCase;
import com.ravn.ecommerce.application.usecases.product.EnableProductUseCase;
import com.ravn.ecommerce.application.usecases.product.command.DeleteProductCommand;
import com.ravn.ecommerce.application.usecases.product.command.SwitchEnabledCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProductManagerResolver {

    private final DeleteProductUseCase deleteProductUseCase;
    private final DisableProductUseCase disableProductUseCase;
    private final EnableProductUseCase enableProductUseCase;

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public Boolean deleteProduct(@Argument Long productId) {
        Long managerId = extractUserId();
        log.info("GraphQL: manager {} deleting product {}", managerId, productId);
        deleteProductUseCase.execute(new DeleteProductCommand(productId, managerId));
        return true;
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ProductResponse disableProduct(@Argument Long productId) {
        Long managerId = extractUserId();
        log.info("GraphQL: manager {} disabling product {}", managerId, productId);
        return disableProductUseCase.execute(new SwitchEnabledCommand(managerId, productId));
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ProductResponse enableProduct(@Argument Long productId) {
        Long managerId = extractUserId();
        log.info("GraphQL: manager {} enabling product {}", managerId, productId);
        return enableProductUseCase.execute(new SwitchEnabledCommand(managerId, productId));
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
