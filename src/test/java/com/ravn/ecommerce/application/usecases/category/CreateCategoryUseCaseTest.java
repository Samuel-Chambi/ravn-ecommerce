package com.ravn.ecommerce.application.usecases.category;

import com.ravn.ecommerce.application.dto.request.category.CreateCategoryRequest;
import com.ravn.ecommerce.application.dto.response.CategoryResponse;
import com.ravn.ecommerce.application.repositories.CategoryRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.category.command.CreateCategoryCommand;
import com.ravn.ecommerce.domain.exceptions.UnauthorizedException;
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
class CreateCategoryUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CreateCategoryUseCase useCase;

    private static final Long USER_ID = 1L;

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

    private CreateCategoryRequest buildRequest() {
        return CreateCategoryRequest.builder()
                .name("Electronics")
                .build();
    }

    private Category buildSavedCategory() {
        return Category.builder()
                .id(1L)
                .name("Electronics")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create category successfully when user is MANAGER")
    void shouldCreateCategorySuccessfully() {
        User manager = buildUser(UserRole.MANAGER);
        CreateCategoryRequest request = buildRequest();
        CreateCategoryCommand command = new CreateCategoryCommand(request, USER_ID);
        Category savedCategory = buildSavedCategory();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(manager));
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryResponse response = useCase.execute(command);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Electronics");
        assertThat(response.getIsActive()).isTrue();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when user is not found")
    void shouldThrowUnauthorizedWhenUserNotFound() {
        CreateCategoryRequest request = buildRequest();
        CreateCategoryCommand command = new CreateCategoryCommand(request, USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when user is not a MANAGER")
    void shouldThrowUnauthorizedWhenUserIsNotManager() {
        User client = buildUser(UserRole.CLIENT);
        CreateCategoryRequest request = buildRequest();
        CreateCategoryCommand command = new CreateCategoryCommand(request, USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("MANAGER");
    }
}
