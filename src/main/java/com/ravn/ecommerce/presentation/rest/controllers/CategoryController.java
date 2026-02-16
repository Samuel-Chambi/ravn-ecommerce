package com.ravn.ecommerce.presentation.rest.controllers;

import com.ravn.ecommerce.application.dto.request.category.CreateCategoryRequest;
import com.ravn.ecommerce.application.dto.request.category.UpdateCategoryRequest;
import com.ravn.ecommerce.application.dto.response.CategoryResponse;
import com.ravn.ecommerce.application.dto.response.PagedCategoryResponse;
import com.ravn.ecommerce.application.services.CurrentUserService;
import com.ravn.ecommerce.application.useCases.category.*;
import com.ravn.ecommerce.application.useCases.category.command.CreateCategoryCommand;
import com.ravn.ecommerce.application.useCases.category.command.DeleteCategoryCommand;
import com.ravn.ecommerce.application.useCases.category.command.UpdateCategoryCommand;
import com.ravn.ecommerce.application.useCases.category.query.ListCategoriesQuery;
import com.ravn.ecommerce.domain.model.product.Category;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final ListCategoriesUseCase listCategoriesUseCase;
    private final GetCategoryByIdUseCase getCategoryByIdUseCase;
    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<PagedCategoryResponse> getAllCategories(
            @RequestParam(required = false, defaultValue = "") String cursor,
            @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        ListCategoriesQuery query = new ListCategoriesQuery(cursor, limit);
        return ResponseEntity.ok(listCategoriesUseCase.execute(query));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(getCategoryByIdUseCase.execute(categoryId));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestBody @Valid CreateCategoryRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        CreateCategoryCommand command = new CreateCategoryCommand(request, userId);
        CategoryResponse category = createCategoryUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategoryById(
            @RequestBody @Valid UpdateCategoryRequest request,
            @PathVariable Long categoryId) {
        Long userId = currentUserService.getCurrentUserId();
        UpdateCategoryCommand command = new UpdateCategoryCommand(request, userId, categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(updateCategoryUseCase.execute(command));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategoryById(
            @PathVariable Long categoryId
    ) {
        Long userId = currentUserService.getCurrentUserId();
        DeleteCategoryCommand command = new DeleteCategoryCommand(categoryId, userId);
        deleteCategoryUseCase.execute(command);
        return ResponseEntity.notFound().build();
    }
}
