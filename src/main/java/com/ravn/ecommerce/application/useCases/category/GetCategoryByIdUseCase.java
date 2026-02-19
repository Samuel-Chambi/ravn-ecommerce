package com.ravn.ecommerce.application.usecases.category;

import com.ravn.ecommerce.application.dto.response.CategoryResponse;
import com.ravn.ecommerce.application.repositories.CategoryRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetCategoryByIdUseCase implements UseCase<Long, CategoryResponse> {
    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse execute(Long categoryId) {
        return categoryRepository.findById(categoryId).map(CategoryResponse::toDto).orElseThrow(
                () -> new EntityNotFoundException("Category ID does not exist"));
    }
}
