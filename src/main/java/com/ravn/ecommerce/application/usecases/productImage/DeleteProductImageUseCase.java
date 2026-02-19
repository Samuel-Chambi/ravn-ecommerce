package com.ravn.ecommerce.application.usecases.productImage;

import com.ravn.ecommerce.application.repositories.ProductImageRepository;
import com.ravn.ecommerce.application.services.ImageStorageService;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.usecases.productImage.commands.DeleteProductImageCommand;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.model.product.ProductImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

/**
 * Use case for deleting product images.
 * Deletes from storage and database, auto-promotes new primary if needed.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DeleteProductImageUseCase implements UseCase<DeleteProductImageCommand, Void> {
    private final ProductImageRepository productImageRepository;
    private final ImageStorageService imageStorageService;

    @Transactional
    @Override
    public Void execute(DeleteProductImageCommand command) {
        Long imageId = command.imageId();
        Long productId = command.productId();
        log.info("Deleting image {} to product {}", imageId, productId);

        // Verify image exists and belongs to product
        ProductImage productImage = productImageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("ProductImage", imageId));
        if (!productImage.getProductId().equals(productId)) {
            throw new EntityNotFoundException("ProductImage", imageId);
        }

        boolean isPrimary = productImage.isPrimary();

        // Delete from storage (local or S3)
        try {
            imageStorageService.delete(productImage.getImageUrl());
        } catch (IOException e) {
            log.warn("Failed to delete image file: {}", productImage.getImageUrl());
            // Continue with database deletion even if storage fails
        }

        // Delete from database
        productImageRepository.deleteById(imageId);

        // If deleted image was primary, promote first remaining image
        if (isPrimary) {
            List<ProductImage> productImages = productImageRepository.findByProductId(productId);
            if (!productImages.isEmpty()) {
                ProductImage newFirst = productImages.getFirst();
                newFirst.markAsPrimary();
                productImageRepository.save(newFirst);
                log.info("Set image {} as new primary for product {}", newFirst.getImageUrl(), productId);
            }
        }

        log.info("Successfully deleted image {} for product {}", imageId, productId);
        return null;
    }
}
