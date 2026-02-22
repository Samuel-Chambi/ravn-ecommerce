package com.ravn.ecommerce.infrastructure.events;

import com.ravn.ecommerce.application.repositories.LikeRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.services.EmailService;
import com.ravn.ecommerce.domain.model.order.events.OrderStatusChangedEvent;
import com.ravn.ecommerce.domain.model.product.Like;
import com.ravn.ecommerce.domain.model.product.events.LowStockEvent;
import com.ravn.ecommerce.domain.model.product.events.ProductDiscountedEvent;
import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.events.PasswordChangedEvent;
import com.ravn.ecommerce.application.usecases.user.SendPasswordChangeEmailUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final EmailService emailService;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final SendPasswordChangeEmailUseCase sendPasswordChangeEmailUseCase;

    // ── Password Changed ──────────────────────────────────────────────────────

    @Async
    @EventListener
    public void onPasswordChanged(PasswordChangedEvent event) {
        log.info("Sending password-changed email to {}", event.getEmail());
        sendPasswordChangeEmailUseCase.execute(event.getEmail(), event.getOccurredOn());
    }

    // ── Order Status Changed ──────────────────────────────────────────────────

    @Async
    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("Sending order-status email for order {} to {}", event.getOrderId(), event.getUserEmail());
        emailService.sendHtmlEmail(
                event.getUserEmail(),
                "Order #" + event.getOrderId() + " Status Update",
                "order-status",
                Map.of(
                        "orderId", event.getOrderId(),
                        "status", event.getNewStatus().name(),
                        "statusLabel", formatStatus(event.getNewStatus().name()),
                        "email", event.getUserEmail()));
    }

    // ── Low Stock Alert ───────────────────────────────────────────────────────

    @Async
    @EventListener
    public void onLowStock(LowStockEvent event) {
        log.info("Sending low-stock alerts for product {}", event.getProductId());
        List<Like> likes = likeRepository.findAllByProductId(event.getProductId());
        likes.forEach(like -> {
            Optional<User> userOpt = userRepository.findById(like.getUserId());
            userOpt.ifPresent(user -> {
                log.debug("Notifying user {} of low stock on product {}", user.getEmail(), event.getProductName());
                emailService.sendHtmlEmail(
                        user.getEmail(),
                        "Hurry! '" + event.getProductName() + "' is almost gone!",
                        "low-stock",
                        Map.of(
                                "productName", event.getProductName(),
                                "stock", event.getCurrentStock(),
                                "email", user.getEmail()));
            });
        });
    }

    // ── Discount Alert ────────────────────────────────────────────────────────

    @Async
    @EventListener
    public void onProductDiscounted(ProductDiscountedEvent event) {
        log.info("Sending discount alerts for product {}", event.getProductId());
        List<Like> likes = likeRepository.findAllByProductId(event.getProductId());

        BigDecimal savingsPct = event.getOldPrice()
                .subtract(event.getNewPrice())
                .multiply(BigDecimal.valueOf(100))
                .divide(event.getOldPrice(), 0, RoundingMode.HALF_UP);

        likes.forEach(like -> {
            Optional<User> userOpt = userRepository.findById(like.getUserId());
            userOpt.ifPresent(user -> {
                log.debug("Notifying user {} of discount on product {}", user.getEmail(), event.getProductName());
                emailService.sendHtmlEmail(
                        user.getEmail(),
                        "🎉 Your liked item '" + event.getProductName() + "' is now " + savingsPct + "% off!",
                        "discount-alert",
                        Map.of(
                                "productName", event.getProductName(),
                                "oldPrice", event.getOldPrice(),
                                "newPrice", event.getNewPrice(),
                                "savingsPct", savingsPct,
                                "email", user.getEmail()));
            });
        });
    }

    private String formatStatus(String status) {
        return switch (status) {
            case "PENDING" -> "Pending";
            case "CONFIRMED" -> "Confirmed";
            case "SHIPPED" -> "Shipped";
            case "DELIVERED" -> "Delivered";
            case "CANCELLED" -> "Cancelled";
            default -> status;
        };
    }
}
