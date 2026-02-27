package com.ravn.ecommerce.presentation.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.ravn.ecommerce.application.dto.response.ProductImageResponse;
import com.ravn.ecommerce.application.usecases.productImage.UploadProductImageUseCase;
import com.ravn.ecommerce.application.usecases.productImage.DeleteProductImageUseCase;
import com.ravn.ecommerce.application.usecases.productImage.GetProductImagesUseCase;
import com.ravn.ecommerce.application.usecases.productImage.commands.DeleteProductImageCommand;
import com.ravn.ecommerce.application.usecases.productImage.commands.UploadProductImageCommand;
import com.ravn.ecommerce.application.validation.ValidImageFiles;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
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
@Tag(name = "Product Images", description = "Endpoints for uploading, retrieving, and managing product images")
public class ProductImageController {
    private final UploadProductImageUseCase uploadProductImageUseCase;
    private final GetProductImagesUseCase getProductImagesUseCase;
    private final DeleteProductImageUseCase deleteProductImageUseCase;

    @Operation(summary = "Get product images", description = "Retrieves all images associated with a specific product.")
    @GetMapping
    public ResponseEntity<List<ProductImageResponse>> getProductImages(@PathVariable Long productId) {
        List<ProductImageResponse> images = getProductImagesUseCase.execute(productId);
        return ResponseEntity.ok(images);
    }

    @Operation(summary = "Upload product images", description = "Uploads one or more images for a specific product. Can optionally set the first uploaded image as the primary image.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ProductImageResponse>> uploadImages(
            @PathVariable Long productId,
            @RequestParam("images") @NotEmpty @ValidImageFiles List<MultipartFile> files,
            @RequestParam(value = "markFirstAsPrimary", required = false, defaultValue = "true") Boolean markFirstAsPrimary) {
        List<ProductImageResponse> uploadedImages = uploadProductImageUseCase.execute(new UploadProductImageCommand(
                productId,
                files,
                markFirstAsPrimary));
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedImages);
    }

    @Operation(summary = "Delete product image", description = "Deletes a specific image from a product. Requires MANAGER role.")
    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        DeleteProductImageCommand command = new DeleteProductImageCommand(productId, imageId);
        deleteProductImageUseCase.execute(command);
        return ResponseEntity.noContent().build();
    }
}
