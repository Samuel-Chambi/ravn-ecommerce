package com.ravn.ecommerce.presentation.rest.controllers;

import com.ravn.ecommerce.application.dto.request.product.ProductRequest;
import com.ravn.ecommerce.application.dto.request.product.UpdateProductRequest;
import com.ravn.ecommerce.application.dto.response.PagedProductResponse;
import com.ravn.ecommerce.application.dto.response.ProductResponse;
import com.ravn.ecommerce.application.services.CurrentUserService;
import com.ravn.ecommerce.application.useCases.product.*;
import com.ravn.ecommerce.application.useCases.product.command.CreateProductCommand;
import com.ravn.ecommerce.application.useCases.product.command.DeleteProductCommand;
import com.ravn.ecommerce.application.useCases.product.command.UpdateProductCommand;
import com.ravn.ecommerce.application.useCases.product.query.ListProductsQuery;
import com.ravn.ecommerce.domain.model.product.Product;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ListProductsUseCase listProductsUseCase;
    private final GetProductByIdUseCase getProductByIdUseCase;
    private final CreateProductUseCase createProductUseCase;
    private final CurrentUserService currentUserService;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;

    @GetMapping
    public ResponseEntity<PagedProductResponse> getProducts(
            @RequestParam(required = false, defaultValue = "") String cursor,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false, defaultValue = "1") Long categoryId,
            @RequestParam(required = false, defaultValue = "") String search,
            // TODO: This parameter should probably be restricted to Admins in the future
            @RequestParam(defaultValue = "false") boolean showDisabled) {
        ListProductsQuery query = new ListProductsQuery(cursor, limit, sortBy, sortDirection, categoryId, search,
                showDisabled);
        return ResponseEntity.ok(listProductsUseCase.execute(query));
    }


    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long productId) {
        return ResponseEntity.ok(getProductByIdUseCase.execute(productId));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest productRequest) {
        Long userId = currentUserService.getCurrentUserId();
        CreateProductCommand command = new CreateProductCommand(productRequest, userId);
        ProductResponse product = createProductUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> updateProductById(
            @PathVariable Long productId,
            @RequestBody UpdateProductRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        UpdateProductCommand command = new UpdateProductCommand(productId, request, userId);
        ProductResponse product = updateProductUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.OK).body(product);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProductById(
            @PathVariable Long productId) {
        Long userId = currentUserService.getCurrentUserId();
        DeleteProductCommand command = new DeleteProductCommand(productId, userId);
        deleteProductUseCase.execute(command);
        return ResponseEntity.noContent().build();
    }

}
