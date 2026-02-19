package com.ravn.ecommerce.application.usecases.product;

import com.ravn.ecommerce.application.dto.response.ProductResponse;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.domain.exceptions.ProductNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class GetProductUseCase implements UseCase<Long, ProductResponse> {
    private final ProductRepository productRepository;

    @Override
    public ProductResponse execute(Long productId) {
        // Product exists
        return productRepository.findById(productId).map(ProductResponse::toDto)
                .orElseThrow(() -> new ProductNotFound("Product ID " + productId + " does not exist"));
    }
}
