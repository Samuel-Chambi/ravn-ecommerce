package com.ravn.ecommerce.presentation.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.ravn.ecommerce.application.dto.request.product.ProductRequest;
import com.ravn.ecommerce.application.dto.request.product.UpdateProductRequest;
import com.ravn.ecommerce.application.dto.response.PagedProductResponse;
import com.ravn.ecommerce.application.dto.response.ProductResponse;
import com.ravn.ecommerce.application.services.CurrentUserService;
import com.ravn.ecommerce.application.usecases.product.*;
import com.ravn.ecommerce.application.usecases.product.command.CreateProductCommand;
import com.ravn.ecommerce.application.usecases.product.command.DeleteProductCommand;
import com.ravn.ecommerce.application.usecases.product.command.UpdateProductCommand;
import com.ravn.ecommerce.application.usecases.product.query.ListProductsQuery;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Public endpoints for browsing, searching, and viewing products")
public class ProductController {

    private final ListProductsUseCase listProductsUseCase;
    private final GetProductByIdUseCase getProductByIdUseCase;
    private final CreateProductUseCase createProductUseCase;
    private final CurrentUserService currentUserService;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;

    @Operation(summary = "List/Search products", description = "Retrieves a paginated list of active products. Supports sorting, filtering by category, and text search.")
    @GetMapping
    public ResponseEntity<PagedProductResponse> getProducts(
            @RequestParam(required = false, defaultValue = "") String cursor,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "") String search) {
        ListProductsQuery query = new ListProductsQuery(cursor, limit, sortBy, sortDirection, categoryId, search,
                false);
        return ResponseEntity.ok(listProductsUseCase.execute(query));
    }

    @Operation(summary = "Get product details", description = "Retrieves the full details of a specific product by its ID.")
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long productId) {
        return ResponseEntity.ok(getProductByIdUseCase.execute(productId));
    }

    @Operation(summary = "Create product (Admin)", description = "Creates a new product in the catalog. Requires MANAGER role.")
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest productRequest) {
        Long userId = currentUserService.getCurrentUserId();
        CreateProductCommand command = new CreateProductCommand(productRequest, userId);
        ProductResponse product = createProductUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @Operation(summary = "Update product (Admin)", description = "Updates an existing product's details. Requires MANAGER role.")
    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductResponse> updateProductById(
            @PathVariable Long productId,
            @RequestBody UpdateProductRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        UpdateProductCommand command = new UpdateProductCommand(productId, request, userId);
        ProductResponse product = updateProductUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.OK).body(product);
    }

    @Operation(summary = "Delete product (Admin)", description = "Soft-deletes an existing product. Requires MANAGER role.")
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteProductById(
            @PathVariable Long productId) {
        Long userId = currentUserService.getCurrentUserId();
        DeleteProductCommand command = new DeleteProductCommand(productId, userId);
        deleteProductUseCase.execute(command);
        return ResponseEntity.noContent().build();
    }

}
