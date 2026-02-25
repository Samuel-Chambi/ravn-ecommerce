package com.ravn.ecommerce.presentation.graphql;

import com.ravn.ecommerce.application.dto.response.OrderResponse;
import com.ravn.ecommerce.application.usecases.order.*;
import com.ravn.ecommerce.application.usecases.order.command.GetUserOrderByIdCommand;
import com.ravn.ecommerce.application.usecases.order.command.UpdateDeliveryStatusCommand;
import com.ravn.ecommerce.application.usecases.order.command.GetUserOrdersCommand;
import com.ravn.ecommerce.application.usecases.order.command.GetAllOrdersCommand;
import com.ravn.ecommerce.domain.model.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Window;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class OrderResolver {

    private final GetUserOrdersUseCase getUserOrdersUseCase;
    private final GetUserOrderByIdUseCase getUserOrderByIdUseCase;
    private final GetAllOrdersUseCase getAllOrdersUseCase;
    private final GetOrderByIdUseCase getOrderByIdUseCase;
    private final UpdateDeliveryStatusUseCase updateDeliveryStatusUseCase;
    private final CreateOrderFromCartUseCase createOrderFromCartUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final CancelUserOrderUseCase cancelUserOrderUseCase;
    // ── Queries ───────────────────────────────────────────────────────────────

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Window<OrderResponse> myOrders(ScrollSubrange subrange) {
        Long userId = extractUserId();
        ScrollPosition position = subrange.position().orElse(ScrollPosition.keyset());
        Limit limit = Limit.of(subrange.count().orElse(10));
        return getUserOrdersUseCase.execute(new GetUserOrdersCommand(userId, position, limit));
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public OrderResponse myOrderById(@Argument Long orderId) {
        Long userId = extractUserId();
        return getUserOrderByIdUseCase.execute(new GetUserOrderByIdCommand(userId, orderId));
    }

    @QueryMapping
    @PreAuthorize("hasRole('MANAGER')")
    public Window<OrderResponse> allOrders(ScrollSubrange subrange) {
        ScrollPosition position = subrange.position().orElse(ScrollPosition.keyset());
        Limit limit = Limit.of(subrange.count().orElse(10));
        return getAllOrdersUseCase.execute(new GetAllOrdersCommand(position, limit));
    }

    @QueryMapping
    @PreAuthorize("hasRole('MANAGER')")
    public OrderResponse orderById(@Argument Long orderId) {
        return getOrderByIdUseCase.execute(orderId);
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public OrderResponse createOrderFromCart() {
        Long userId = extractUserId();
        return createOrderFromCartUseCase.execute(userId);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public boolean cancelUserOrderById(
            @Argument Long orderId) {
        Long userId = extractUserId();
        cancelUserOrderUseCase.execute(new GetUserOrderByIdCommand(userId, orderId));
        return true;
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public OrderResponse updateDeliveryStatus(
            @Argument Long orderId,
            @Argument OrderStatus status) {

        Long managerId = extractUserId();
        return updateDeliveryStatusUseCase.execute(
                new UpdateDeliveryStatusCommand(orderId, managerId, status));
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public boolean cancelOrderById(
            @Argument Long orderId) {
        cancelOrderUseCase.execute(orderId);
        return true;
    }
    // ── Helpers ───────────────────────────────────────────────────────────────

    private Long extractUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new IllegalStateException("User is not authenticated");
        }
        return Long.parseLong(auth.getName());
    }
}
