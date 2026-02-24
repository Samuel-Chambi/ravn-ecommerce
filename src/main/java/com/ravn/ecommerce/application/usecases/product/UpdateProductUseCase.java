package com.ravn.ecommerce.application.usecases.product;

import com.ravn.ecommerce.application.dto.response.ProductResponse;
import com.ravn.ecommerce.application.events.EventPublisher;
import com.ravn.ecommerce.application.repositories.CategoryRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.usecases.product.command.UpdateProductCommand;
import com.ravn.ecommerce.domain.exceptions.*;
import com.ravn.ecommerce.domain.model.product.Inventory;
import com.ravn.ecommerce.domain.model.product.Product;
import com.ravn.ecommerce.domain.model.product.events.LowStockEvent;
import com.ravn.ecommerce.domain.model.product.events.ProductDiscountedEvent;
import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateProductUseCase implements UseCase<UpdateProductCommand, ProductResponse> {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    private static final int LOW_STOCK_THRESHOLD = 3;

    @Override
    @Transactional
    public ProductResponse execute(UpdateProductCommand command) {
        log.info("Updating product ID: {}", command.productId());

        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFound("User not found"));

        if (user.getRole() != UserRole.MANAGER) {
            throw new UnauthorizedException("Only managers can update products");
        }

        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFound(
                        String.format("Product not found with id: %d", command.productId())));

        var request = command.request();

        if (request.getName() != null) {
            product.setName(request.getName());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        if (request.getPrice() != null) {
            BigDecimal oldPrice = product.getPrice();
            product.updatePrice(request.getPrice());

            // Discount alert — new price is lower than old price
            if (request.getPrice().compareTo(oldPrice) < 0) {
                log.info("Price reduced for product {} — firing discount alert", product.getId());
                eventPublisher.publish(new ProductDiscountedEvent(
                        product.getId(), product.getName(), oldPrice, request.getPrice()));
            }
        }

        if (request.getCategoryId() != null) {
            categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getCategoryId()));
            product.setCategoryId(request.getCategoryId());
        }

        if (request.getEnabled() != null) {
            if (request.getEnabled())
                product.enable();
            else
                product.disable();
        }

        if (request.getStock() != null) {
            if(product.getInventory() == null){
                product.setInventory(
                        Inventory.builder()
                                .productId(product.getId())
                                .updatedAt(LocalDateTime.now())
                                .quantity(0)
                                .build());
            }
            product.getInventory().setQuantity(request.getStock());

            // Low stock alert (triggers only when dropping below threshold)
            if (request.getStock() < LOW_STOCK_THRESHOLD) {
                log.info("Low stock ({}) detected for product {} — firing alert", request.getStock(), product.getId());
                eventPublisher.publish(new LowStockEvent(
                        product.getId(), product.getName(), request.getStock()));
            }
        }

        product.setUpdatedAt(LocalDateTime.now());
        Product updated = productRepository.save(product);
        log.info("Product {} updated successfully", updated.getId());

        return ProductResponse.toDto(updated);
    }
}
