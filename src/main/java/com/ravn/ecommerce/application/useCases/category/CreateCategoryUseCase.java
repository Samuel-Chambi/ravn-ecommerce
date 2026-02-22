package com.ravn.ecommerce.application.useCases.category;

import com.ravn.ecommerce.application.dto.request.category.CreateCategoryRequest;
import com.ravn.ecommerce.application.dto.response.CategoryResponse;
import com.ravn.ecommerce.application.repositories.CategoryRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.useCases.UseCase;
import com.ravn.ecommerce.application.useCases.category.command.CreateCategoryCommand;
import com.ravn.ecommerce.domain.exceptions.UnauthorizedException;
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
public class CreateCategoryUseCase implements UseCase<CreateCategoryCommand, CategoryResponse> {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CategoryResponse execute(CreateCategoryCommand command) {
        log.info("Creating category: {}", command.request().getName());
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        if (user.getRole() != UserRole.MANAGER) {
            throw new UnauthorizedException("Only MANAGERs can create categories");
        }

        Category category = Category.builder()
                .name(command.request().getName())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", saved.getId());
        return CategoryResponse.toDto(saved);
    }
}
