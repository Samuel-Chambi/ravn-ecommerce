package com.ravn.ecommerce.application.useCases.product;

import com.ravn.ecommerce.application.dto.response.ProductImageResponse;
import com.ravn.ecommerce.application.repositories.ProductImageRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.services.ImageStorageService;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.InvalidOperationException;
import com.ravn.ecommerce.domain.model.product.ProductImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Use case for uploading product images.
 * Validates images, enforces max limit (5 per product), and stores using
 * ImageStorageService.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UploadProductImageUseCase {
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ImageStorageService imageStorageService;
    private static final int MAX_IMAGES_PER_PRODUCT = 5;

    @Transactional
    public List<ProductImageResponse> execute(Long productId, List<MultipartFile> files,
            Boolean markFirstAsPrimary) {
        int newFiles = files.size();
        log.info("Uploading {} images, for product {}", newFiles, productId);

        // Verify product exists
        productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));

        // Enforce max images limit
        int currentImageCount = productImageRepository.countByProductId(productId);
        if (currentImageCount + newFiles > MAX_IMAGES_PER_PRODUCT) {
            throw new InvalidOperationException(
                    String.format("Cannot upload %d images. Product already has %d images. Maximum allowed: %d",
                            newFiles,
                            currentImageCount,
                            MAX_IMAGES_PER_PRODUCT),
                    "MAX_IMAGES_EXCEEDED");
        }

        // Note: Image validation is handled by @ValidImageFiles annotation in
        // controller

        // Upload and save images
        List<ProductImageResponse> uploadedImages = new ArrayList<>();
        Boolean shouldMarkAsPrimary = Boolean.TRUE.equals(markFirstAsPrimary) && currentImageCount == 0;

        for (int i = 0; i < newFiles; i++) {
            MultipartFile file = files.get(i);
            try {
                // Store image using configured storage service (local or S3)
                String imageUrl = imageStorageService.store(file, productId);
                boolean isPrimary = shouldMarkAsPrimary && (i == 0);

                // Save to database
                ProductImage productImage = ProductImage.create(productId, imageUrl, isPrimary);
                ProductImage saved = productImageRepository.save(productImage);
                ProductImageResponse dto = ProductImageResponse.toDto(saved);
                uploadedImages.add(dto);

                log.info("Successfully uploaded image: {} for product: {}", imageUrl, productId);
            } catch (IOException e) {
                log.error("Failed to upload image for product {}: {}", productId, e.getMessage());
                throw new InvalidOperationException(
                        "Failed to upload image: " + file.getOriginalFilename(),
                        "IMAGE_UPLOAD_FAILED");
            }
        }
        return uploadedImages;
    }
}
