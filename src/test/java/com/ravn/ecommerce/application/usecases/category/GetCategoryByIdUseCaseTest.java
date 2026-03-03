package com.ravn.ecommerce.application.usecases.category;

import com.ravn.ecommerce.application.dto.response.CategoryResponse;
import com.ravn.ecommerce.application.repositories.CategoryRepository;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.model.product.Category;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCategoryByIdUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private GetCategoryByIdUseCase useCase;

    private static final Long CATEGORY_ID = 1L;

    private Category buildCategory() {
        return Category.builder()
                .id(CATEGORY_ID)
                .name("Electronics")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should return category response when category is found")
    void shouldReturnCategoryWhenFound() {
        Category category = buildCategory();

        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));

        CategoryResponse response = useCase.execute(CATEGORY_ID);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(CATEGORY_ID);
        assertThat(response.getName()).isEqualTo("Electronics");
        assertThat(response.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when category does not exist")
    void shouldThrowEntityNotFoundWhenCategoryDoesNotExist() {
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(CATEGORY_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Category ID does not exist");
    }
}
