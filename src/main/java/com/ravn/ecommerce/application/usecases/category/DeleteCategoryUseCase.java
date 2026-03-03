package com.ravn.ecommerce.application.usecases.category;

import com.ravn.ecommerce.application.repositories.CategoryRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.usecases.category.command.DeleteCategoryCommand;
import com.ravn.ecommerce.domain.exceptions.CategoryNotFoundException;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.product.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeleteCategoryUseCase implements UseCase<DeleteCategoryCommand, Void> {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    public Void execute(DeleteCategoryCommand command) {
        Long userId = command.userId();
        if(!userRepository.existsById(userId)){
            throw new UserNotFound(String.format("User ID %d does not exist." , userId));
        }

        Long categoryId = command.categoryId();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(
                        String.format("Category ID %d does not exist." , categoryId)));
        categoryRepository.deleteById(categoryId);
        return null;
    }
}
