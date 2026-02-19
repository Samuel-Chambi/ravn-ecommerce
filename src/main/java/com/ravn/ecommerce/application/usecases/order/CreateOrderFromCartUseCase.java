package com.ravn.ecommerce.application.usecases.order;

import com.ravn.ecommerce.application.dto.response.OrderResponse;
import com.ravn.ecommerce.application.repositories.CartRepository;
import com.ravn.ecommerce.application.repositories.OrderRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.InvalidOrderException;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.cart.Cart;
import com.ravn.ecommerce.domain.model.cart.CartStatus;
import com.ravn.ecommerce.domain.model.order.Order;
import com.ravn.ecommerce.domain.model.order.OrderItem;
import com.ravn.ecommerce.domain.model.order.OrderStatus;
import com.ravn.ecommerce.domain.model.product.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreateOrderFromCartUseCase implements UseCase<Long, OrderResponse> {
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public OrderResponse execute(Long userId) {
        log.info("Creating order from cart for user ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFound(String.format("User ID %d does not exist", userId));
        }
        Cart currentCart = cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Active cart for user ID %d does not exist", userId)));

        if (currentCart.getItems() == null || currentCart.getItems().isEmpty()) {
            throw new InvalidOrderException("Cannot create an order from an empty cart");
        }

        Order order = Order.builder()
                .userId(userId)
                .status(OrderStatus.PENDING)
                .totalAmount(currentCart.getTotal())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        currentCart.getItems().forEach(cartItem -> {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            String.format("Product ID %d not found", cartItem.getProductId())));
            OrderItem orderItem = OrderItem.builder()
                    .productId(cartItem.getProductId())
                    .productName(product.getName())
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            order.addItem(orderItem);
        });

        order.setTotalAmount(order.calculateTotal());
        Order savedOrder = orderRepository.save(order);

        // Mark cart as ORDERED
        currentCart.setStatus(CartStatus.ORDERED);
        currentCart.setUpdatedAt(LocalDateTime.now());


        cartRepository.save(currentCart);

        log.info("Order created successfully with ID: {} for user ID: {}", savedOrder.getId(), userId);
        return OrderResponse.toDto(savedOrder);
    }
}
