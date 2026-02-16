package com.ravn.ecommerce.presentation.rest.controllers;

import com.ravn.ecommerce.application.dto.response.ProductImageResponse;
import com.ravn.ecommerce.application.useCases.productImage.DeleteProductImageUseCase;
import com.ravn.ecommerce.application.useCases.productImage.GetProductImagesUseCase;
import com.ravn.ecommerce.application.useCases.productImage.UploadProductImageUseCase;
import com.ravn.ecommerce.application.useCases.productImage.commands.DeleteProductImageCommand;
import com.ravn.ecommerce.application.useCases.productImage.commands.UploadProductImageCommand;
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
public class ProductImageController {
    private final UploadProductImageUseCase uploadProductImageUseCase;
    private final GetProductImagesUseCase getProductImagesUseCase;
    private final DeleteProductImageUseCase deleteProductImageUseCase;

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
        UploadProductImageCommand command = new UploadProductImageCommand(productId, files, markFirstAsPrimary);
        List<ProductImageResponse> uploadedImages = uploadProductImageUseCase.execute(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedImages);
    }

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
