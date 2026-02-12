package com.ravn.ecommerce.presentation.rest.controllers;

import com.ravn.ecommerce.application.dto.response.ProductImageResponse;
import com.ravn.ecommerce.application.useCases.product.DeleteProductImagesUseCase;
import com.ravn.ecommerce.application.useCases.product.GetProductImagesUseCase;
import com.ravn.ecommerce.application.useCases.product.UploadProductImageUseCase;
import com.ravn.ecommerce.application.validation.ValidImageFiles;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {
    private final UploadProductImageUseCase uploadProductImageUseCase;
    private final GetProductImagesUseCase getProductImagesUseCase;
    private final DeleteProductImagesUseCase deleteProductImagesUseCase;

    @GetMapping
    public ResponseEntity<List<ProductImageResponse>> getProductImages(@PathVariable Long productId) {
        List<ProductImageResponse> images = getProductImagesUseCase.execute(productId);
        return ResponseEntity.ok(images);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ProductImageResponse>> uploadImages(
            @PathVariable Long productId,
            @RequestParam("images") @NotEmpty @ValidImageFiles List<MultipartFile> files,
            @RequestParam(value = "markFirstAsPrimary", required = false, defaultValue = "true") Boolean markFirstAsPrimary) {
        List<ProductImageResponse> uploadedImages = uploadProductImageUseCase.execute(
                productId,
                files,
                markFirstAsPrimary);

        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedImages);
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        deleteProductImagesUseCase.execute(productId, imageId);
        return ResponseEntity.noContent().build();
    }
}
