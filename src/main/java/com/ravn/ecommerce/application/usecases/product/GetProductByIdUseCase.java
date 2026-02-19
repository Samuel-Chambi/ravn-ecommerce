package com.ravn.ecommerce.application.useCases.product;

import com.ravn.ecommerce.application.dto.response.ProductResponse;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.useCases.UseCase;
import com.ravn.ecommerce.domain.exceptions.ProductNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Use case to retrieve a single product by its ID.
 */
@Service
@RequiredArgsConstructor
public class GetProductByIdUseCase implements UseCase<Long, ProductResponse> {

    private final ProductRepository productRepository;

    @Override
    public ProductResponse execute(Long productId) {
        return productRepository.findById(productId).map(ProductResponse::toDto)
                .orElseThrow(() -> new ProductNotFound("Product ID " + productId + " does not exist"));
    }
}
