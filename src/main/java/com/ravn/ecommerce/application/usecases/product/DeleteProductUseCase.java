package com.ravn.ecommerce.application.useCases.product;

import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.useCases.UseCase;
import com.ravn.ecommerce.application.useCases.product.command.DeleteProductCommand;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.UnauthorizedException;
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
public class DeleteProductUseCase implements UseCase<DeleteProductCommand, Void> {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    @Override
    public Void execute(DeleteProductCommand command) {
        Long userId = command.userId();
        Long productId = command.productId();
        log.info("Deleting product ID: {}", productId);
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (user.getRole() != UserRole.MANAGER) {
            throw new UnauthorizedException("Only managers can delete a product.");
        }

        Product product = productRepository.findById(productId).orElseThrow(() -> new EntityNotFoundException("Product not found"));

        productRepository.deleteById(productId);

        log.info("Successful delete product {}", productId);
        return null;
    }
}
