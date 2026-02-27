package com.ravn.ecommerce.presentation.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.ravn.ecommerce.application.dto.response.PagedProductResponse;
import com.ravn.ecommerce.application.dto.response.ProductResponse;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.usecases.product.ListProductsUseCase;
import com.ravn.ecommerce.application.usecases.product.query.ListProductsQuery;
import com.ravn.ecommerce.domain.exceptions.ProductNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Manager-only product endpoints.
 * Returns ALL products including disabled and out-of-stock items.
 */
@RestController
@RequestMapping("/products/manage")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
@Tag(name = "Product Management (Admin)", description = "Manager-only endpoints for comprehensive product administration")
public class ProductManagerController {

    private final ListProductsUseCase listProductsUseCase;
    private final ProductRepository productRepository;

    @Operation(summary = "Get all products (Admin view)", description = "Retrieves a paginated list of ALL products, including disabled and out-of-stock items, unlike the public endpoint.")
    @GetMapping
    public ResponseEntity<PagedProductResponse> getAllProducts(
            @RequestParam(required = false, defaultValue = "") String cursor,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "") String search) {
        ListProductsQuery query = new ListProductsQuery(cursor, limit, sortBy, sortDirection, categoryId, search,
                true);
        return ResponseEntity.ok(listProductsUseCase.execute(query));
    }

    @Operation(summary = "Get any product by ID (Admin view)", description = "Retrieves details of any specific product, including disabled ones, by its ID.")
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long productId) {
        return productRepository.findById(productId)
                .map(ProductResponse::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ProductNotFound("Product ID " + productId + " does not exist"));
    }
}
