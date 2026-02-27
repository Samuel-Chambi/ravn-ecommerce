package com.ravn.ecommerce.application.usecases.product;

import com.ravn.ecommerce.application.dto.response.ProductResponse;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.domain.exceptions.ProductNotFound;
import com.ravn.ecommerce.domain.model.product.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Use case to retrieve a single product by its ID.
 * Only returns enabled products — disabled products are treated as not found.
 */
@Service
@RequiredArgsConstructor
public class GetProductByIdUseCase implements UseCase<Long, ProductResponse> {

    private final ProductRepository productRepository;

    @Override
    public ProductResponse execute(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFound("Product ID " + productId + " does not exist"));

        if (!product.isEnabled()) {
            throw new ProductNotFound("Product ID " + productId + " does not exist");
        }

        return ProductResponse.toDto(product);
    }
}
