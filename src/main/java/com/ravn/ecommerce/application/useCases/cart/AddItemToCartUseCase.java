package com.ravn.ecommerce.application.usecases.cart;

import com.ravn.ecommerce.application.dto.response.CartResponse;
import com.ravn.ecommerce.application.repositories.CartRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.usecases.cart.command.AddItemToCartCommand;
import com.ravn.ecommerce.domain.exceptions.BusinessRuleViolation;
import com.ravn.ecommerce.domain.exceptions.InsufficientStockException;
import com.ravn.ecommerce.domain.exceptions.ProductNotFound;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.cart.Cart;
import com.ravn.ecommerce.domain.model.cart.CartItem;
import com.ravn.ecommerce.domain.model.cart.CartStatus;
import com.ravn.ecommerce.domain.model.product.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AddItemToCartUseCase implements UseCase<AddItemToCartCommand, CartResponse> {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public CartResponse execute(AddItemToCartCommand command) {
        Long userId = command.userId();
        Long productId = command.productId();
        Integer quantity = command.quantity();
        if (!userRepository.existsById(userId)) {
            throw new UserNotFound(String.format("User ID %d does not exist", userId));
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFound(String.format("Product ID %d does not exist", productId)));

        if (!product.isEnabled()) {
            throw new BusinessRuleViolation(String.format("Product ID %d is not enabled", productId));
        }

        Cart currentCart = cartRepository.findByStatusAndUserId(CartStatus.ACTIVE, userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .userId(userId)
                            .total(BigDecimal.ZERO)
                            .status(CartStatus.ACTIVE)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return cartRepository.save(newCart);
                });

        int desiredQuantity = quantity;
        Optional<CartItem> existingItem = currentCart.findItemByProductId(productId);
        if (existingItem.isPresent()) {
            desiredQuantity += existingItem.get().getQuantity();
        }

        if (product.getAvailableQuantity() < desiredQuantity) {
            throw new InsufficientStockException(productId.toString(), desiredQuantity, product.getAvailableQuantity());
        }

        CartItem itemAdded = CartItem.builder()
                .cartId(currentCart.getId())
                .productId(productId)
                .productName(product.getName())
                .price(product.getPrice())
                .quantity(quantity)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        currentCart.addItem(itemAdded);
        currentCart.setUpdatedAt(LocalDateTime.now());
        Cart updatedCart = cartRepository.save(currentCart);
        return CartResponse.toDto(updatedCart);
    }

}
