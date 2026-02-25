package com.ravn.ecommerce.application.usecases.product;

import com.ravn.ecommerce.application.dto.response.ProductResponse;
import com.ravn.ecommerce.application.repositories.CategoryRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.usecases.product.command.CreateProductCommand;
import com.ravn.ecommerce.domain.exceptions.CategoryNotFoundException;
import com.ravn.ecommerce.domain.exceptions.UnauthorizedException;
import com.ravn.ecommerce.domain.model.product.Inventory;
import com.ravn.ecommerce.domain.model.product.Product;
import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreateProductUseCase implements UseCase<CreateProductCommand, ProductResponse> {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ProductResponse execute(CreateProductCommand command) {
        log.info("Creating product: {}", command.productRequest().getName());

        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (user.getRole() != UserRole.MANAGER) {
            throw new UnauthorizedException("Only MANAGERs can create products");
        }
        categoryRepository.findById(command.productRequest().getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(command.productRequest().getCategoryId()));

        Product product = Product.builder()
                .name(command.productRequest().getName())
                .description(command.productRequest().getDescription())
                .price(command.productRequest().getPrice())
                .isEnabled(command.productRequest().getEnabled())
                .categoryId(command.productRequest().getCategoryId())
                .createdBy(user.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Inventory inventory = Inventory.builder()
                .quantity(command.productRequest().getStock())
                .updatedAt(LocalDateTime.now()).build();

        product.setInventory(inventory);

        Product saved = productRepository.save(product);
        log.info("Product created successfully with ID: {}", saved.getId());

        return ProductResponse.toDto(saved);
    }
}
