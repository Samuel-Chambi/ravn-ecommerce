package com.ravn.ecommerce.application.usecases.productImage;

import com.ravn.ecommerce.application.dto.response.ProductImageResponse;
import com.ravn.ecommerce.application.repositories.ProductImageRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.model.product.ProductImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
/*
 * Get all product images use case
 * */
public class GetProductImagesUseCase implements UseCase<Long, List<ProductImageResponse>> {
    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    @Override
    public List<ProductImageResponse> execute(Long productId) {
        log.info("Getting images for product {}", productId);
        productRepository.findById(productId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Product", productId));
        List<ProductImage> productImages = productImageRepository.findByProductId(productId);
        return productImages.stream()
                .map(ProductImageResponse::toDto)
                .toList();
    }
}
