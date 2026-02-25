package com.ravn.ecommerce.application.usecases.category;

import com.ravn.ecommerce.application.dto.request.category.UpdateCategoryRequest;
import com.ravn.ecommerce.application.dto.response.CategoryResponse;
import com.ravn.ecommerce.application.repositories.CategoryRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.usecases.category.command.UpdateCategoryCommand;
import com.ravn.ecommerce.domain.exceptions.CategoryNotFoundException;
import com.ravn.ecommerce.domain.exceptions.UnauthorizedException;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.product.Category;
import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateCategoryUseCase implements UseCase<UpdateCategoryCommand, CategoryResponse> {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CategoryResponse execute(UpdateCategoryCommand command) {
        log.info("Updating category ID: {}", command.categoryId());

        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFound("User not found."));
        if (user.getRole() != UserRole.MANAGER) {
            throw new UnauthorizedException("Only MANAGERs can update categories information");
        }

        Category category = categoryRepository.findById(command.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(command.categoryId()));

        UpdateCategoryRequest request = command.request();
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        category.setUpdatedAt(LocalDateTime.now());

        Category updated = categoryRepository.save(category);
        log.info("Category {} updated successfully", category.getName());

        return CategoryResponse.toDto(updated);
    }
}
