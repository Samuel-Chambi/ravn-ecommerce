package com.ravn.ecommerce.application.usecases.category;

import com.ravn.ecommerce.application.dto.request.category.UpdateCategoryRequest;
import com.ravn.ecommerce.application.dto.response.CategoryResponse;
import com.ravn.ecommerce.application.repositories.CategoryRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.category.command.UpdateCategoryCommand;
import com.ravn.ecommerce.domain.exceptions.CategoryNotFoundException;
import com.ravn.ecommerce.domain.exceptions.UnauthorizedException;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.product.Category;
import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCategoryUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UpdateCategoryUseCase useCase;

    private static final Long USER_ID = 1L;
    private static final Long CATEGORY_ID = 1L;

    private User buildUser(UserRole role) {
        return User.builder()
                .id(USER_ID)
                .email("test@example.com")
                .passwordHash("hash")
                .role(role)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Category buildCategory() {
        return Category.builder()
                .id(CATEGORY_ID)
                .name("Electronics")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private UpdateCategoryRequest buildRequest(String name) {
        return UpdateCategoryRequest.builder()
                .name(name)
                .build();
    }

    @Test
    @DisplayName("Should update category successfully when user is MANAGER and category exists")
    void shouldUpdateCategorySuccessfully() {
        User manager = buildUser(UserRole.MANAGER);
        Category category = buildCategory();
        UpdateCategoryRequest request = buildRequest("Updated Electronics");
        UpdateCategoryCommand command = new UpdateCategoryCommand(request, USER_ID, CATEGORY_ID);

        Category updatedCategory = Category.builder()
                .id(CATEGORY_ID)
                .name("Updated Electronics")
                .isActive(true)
                .createdAt(category.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(manager));
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        CategoryResponse response = useCase.execute(command);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(CATEGORY_ID);
        assertThat(response.getName()).isEqualTo("Updated Electronics");
        assertThat(response.getIsActive()).isTrue();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw UserNotFound when user does not exist")
    void shouldThrowUserNotFoundWhenUserDoesNotExist() {
        UpdateCategoryRequest request = buildRequest("Updated Electronics");
        UpdateCategoryCommand command = new UpdateCategoryCommand(request, USER_ID, CATEGORY_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFound.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when user is not a MANAGER")
    void shouldThrowUnauthorizedWhenUserIsNotManager() {
        User client = buildUser(UserRole.CLIENT);
        UpdateCategoryRequest request = buildRequest("Updated Electronics");
        UpdateCategoryCommand command = new UpdateCategoryCommand(request, USER_ID, CATEGORY_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("MANAGER");
    }

    @Test
    @DisplayName("Should throw CategoryNotFoundException when category does not exist")
    void shouldThrowCategoryNotFoundWhenCategoryDoesNotExist() {
        User manager = buildUser(UserRole.MANAGER);
        UpdateCategoryRequest request = buildRequest("Updated Electronics");
        UpdateCategoryCommand command = new UpdateCategoryCommand(request, USER_ID, CATEGORY_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(manager));
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(CategoryNotFoundException.class);
    }
}
