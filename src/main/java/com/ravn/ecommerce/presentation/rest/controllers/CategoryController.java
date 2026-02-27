package com.ravn.ecommerce.presentation.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.ravn.ecommerce.application.dto.request.category.CreateCategoryRequest;
import com.ravn.ecommerce.application.dto.request.category.UpdateCategoryRequest;
import com.ravn.ecommerce.application.dto.response.CategoryResponse;
import com.ravn.ecommerce.application.dto.response.PagedCategoryResponse;
import com.ravn.ecommerce.application.services.CurrentUserService;
import com.ravn.ecommerce.application.usecases.category.*;
import com.ravn.ecommerce.application.usecases.category.command.CreateCategoryCommand;
import com.ravn.ecommerce.application.usecases.category.command.DeleteCategoryCommand;
import com.ravn.ecommerce.application.usecases.category.command.UpdateCategoryCommand;
import com.ravn.ecommerce.application.usecases.category.query.ListCategoriesQuery;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Endpoints for retrieving and managing product categories")
public class CategoryController {
    private final ListCategoriesUseCase listCategoriesUseCase;
    private final GetCategoryByIdUseCase getCategoryByIdUseCase;
    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;
    private final CurrentUserService currentUserService;

    @Operation(summary = "Get all categories", description = "Retrieves a paginated list of all product categories available in the system.")
    @GetMapping
    public ResponseEntity<PagedCategoryResponse> getAllCategories(
            @RequestParam(required = false, defaultValue = "") String cursor,
            @RequestParam(required = false, defaultValue = "10") int limit) {
        ListCategoriesQuery query = new ListCategoriesQuery(cursor, limit);
        return ResponseEntity.ok(listCategoriesUseCase.execute(query));
    }

    @Operation(summary = "Get a category by ID", description = "Retrieves the details of a specific product category by its ID.")
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(getCategoryByIdUseCase.execute(categoryId));
    }

    @Operation(summary = "Create a new category", description = "Creates a new product category. Requires MANAGER role.")
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestBody @Valid CreateCategoryRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        CategoryResponse category = createCategoryUseCase.execute(new CreateCategoryCommand(request, userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @Operation(summary = "Update a category", description = "Updates an existing product category. Requires MANAGER role.")
    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<CategoryResponse> updateCategoryById(
            @RequestBody @Valid UpdateCategoryRequest request,
            @PathVariable Long categoryId) {
        Long userId = currentUserService.getCurrentUserId();
        CategoryResponse response = updateCategoryUseCase
                .execute(new UpdateCategoryCommand(request, userId, categoryId));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Delete a category", description = "Deletes an existing product category. Requires MANAGER role.")
    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteCategoryById(
            @PathVariable Long categoryId) {
        Long userId = currentUserService.getCurrentUserId();
        DeleteCategoryCommand command = new DeleteCategoryCommand(categoryId, userId);
        deleteCategoryUseCase.execute(command);
        return ResponseEntity.noContent().build();
    }
}
