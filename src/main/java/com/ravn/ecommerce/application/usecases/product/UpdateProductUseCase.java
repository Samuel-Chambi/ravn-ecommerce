package com.ravn.ecommerce.application.useCases.product;

import com.ravn.ecommerce.application.dto.response.ProductResponse;
import com.ravn.ecommerce.application.repositories.CategoryRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.useCases.UseCase;
import com.ravn.ecommerce.application.useCases.product.command.UpdateProductCommand;
import com.ravn.ecommerce.domain.exceptions.*;
import com.ravn.ecommerce.domain.model.product.Product;
import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateProductUseCase implements UseCase<UpdateProductCommand, ProductResponse> {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ProductResponse execute(UpdateProductCommand command) {
        log.info("Updating product ID: {}", command.productId());

        // User exists and has MANAGER role
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFound("User not found"));

        if (user.getRole() != UserRole.MANAGER) {
            throw new UnauthorizedException("Only managers can update products");
        }

        // Product exists
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFound(
                        String.format("Product not found with id: %d", command.productId())));

        // Only update 'not-null' Dto's fields
        var request = command.request();

        if (request.getName() != null) {
            product.setName(request.getName());
            log.debug("Updated name to: {}", request.getName());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
            log.debug("Updated description");
        }

        if (request.getPrice() != null) {
            // Valid price
            product.updatePrice(request.getPrice());
            log.debug("Updated price to: {}", request.getPrice());
        }

        if (request.getCategoryID() != null) {
            // Valid category
            categoryRepository.findById(request.getCategoryID())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getCategoryID()));
            product.setCategoryId(request.getCategoryID());
            log.debug("Updated category to: {}", request.getCategoryID());
        }

        if (request.getEnabled() != null) {
            if (request.getEnabled()) {
                product.enable();
            } else {
                product.disable();
            }
            log.debug("Updated enabled status to: {}", request.getEnabled());
        }

        if (request.getStock() != null && product.getInventory() != null) {
            product.getInventory().setQuantity(request.getStock());
            log.debug("Updated stock to: {}", request.getStock());
        }

        // Save
        Product updated = productRepository.save(product);
        log.info("Product {} updated successfully", updated.getId());

        return ProductResponse.toDto(updated);
    }
}
