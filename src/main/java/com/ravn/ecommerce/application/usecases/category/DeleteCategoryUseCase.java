package com.ravn.ecommerce.application.usecases.category;

import com.ravn.ecommerce.application.repositories.CategoryRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.usecases.category.command.DeleteCategoryCommand;
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
        // TODO: Implements soft delete for categories
        return null;
    }
}
