package com.ravn.ecommerce.application.usecases.product;

import com.ravn.ecommerce.application.dto.response.ProductResponse;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.usecases.product.command.SwitchEnabledCommand;
import com.ravn.ecommerce.domain.exceptions.ProductNotFound;
import com.ravn.ecommerce.domain.exceptions.UnauthorizedException;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.product.Product;
import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnableProductUseCase implements UseCase<SwitchEnabledCommand, ProductResponse> {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public ProductResponse execute(SwitchEnabledCommand command) {
        Long userId = command.userId();
        Long productId = command.productId();
        log.info("Enabling product ID {}", productId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound(String.format("User ID %d does not exist", userId)));
        if (user.getRole() != UserRole.MANAGER) {
            throw new UnauthorizedException("Only MANAGERs can enable products");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFound(String.format("Product ID %d does not exist", productId)));
        product.enable();
        Product productEnabled = productRepository.save(product);
        log.info("Product ID {} enabled successfully", productId);
        return ProductResponse.toDto(productEnabled);
    }
}
