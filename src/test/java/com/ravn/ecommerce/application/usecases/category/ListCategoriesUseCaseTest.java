package com.ravn.ecommerce.application.usecases.category;

import com.ravn.ecommerce.application.dto.response.PagedCategoryResponse;
import com.ravn.ecommerce.application.repositories.CategoryRepository;
import com.ravn.ecommerce.application.usecases.category.query.ListCategoriesQuery;
import com.ravn.ecommerce.domain.model.product.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListCategoriesUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ListCategoriesUseCase useCase;

    private Category buildCategory() {
        return Category.builder()
                .id(1L)
                .name("Electronics")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should list categories on first page when cursor is null")
    void shouldListCategoriesOnFirstPage() {
        int limit = 10;
        Category category = buildCategory();
        ListCategoriesQuery query = new ListCategoriesQuery(null, limit);

        Window<Category> window = Window.from(List.of(category), val -> null);
        when(categoryRepository.findBy(any(ScrollPosition.class), eq(limit))).thenReturn(window);

        PagedCategoryResponse response = useCase.execute(query);

        assertThat(response).isNotNull();
        assertThat(response.getCategories()).hasSize(1);
        assertThat(response.getCategories().get(0).getName()).isEqualTo("Electronics");
        assertThat(response.getReturnedCount()).isEqualTo(1);
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getNextCursor()).isNull();
        verify(categoryRepository).findBy(any(ScrollPosition.class), eq(limit));
    }
}
