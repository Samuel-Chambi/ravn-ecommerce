package com.ravn.ecommerce.application.usecases.product;

import com.ravn.ecommerce.application.dto.response.PagedProductResponse;
import com.ravn.ecommerce.application.repositories.CategoryRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.usecases.product.query.ListProductsQuery;
import com.ravn.ecommerce.domain.exceptions.CategoryNotFoundException;
import com.ravn.ecommerce.domain.model.product.Category;
import com.ravn.ecommerce.domain.model.product.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListProductsUseCaseTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ListProductsUseCase useCase;

    private Product buildProduct(Long id, String name) {
        return Product.builder()
                .id(id)
                .name(name)
                .price(new BigDecimal("25.00"))
                .isEnabled(true)
                .categoryId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should return enabled products for first page without filters")
    void shouldReturnEnabledProductsFirstPage() {
        ListProductsQuery query = new ListProductsQuery(null, 10, "id", "asc", null, null, false);
        List<Product> products = List.of(buildProduct(1L, "Product A"), buildProduct(2L, "Product B"));
        Window<Product> window = Window.from(products, ScrollPosition::offset);

        when(productRepository.findByIsEnabled(eq(true), any(ScrollPosition.class), eq(10), any(Sort.class)))
                .thenReturn(window);

        PagedProductResponse response = useCase.execute(query);

        assertThat(response).isNotNull();
        assertThat(response.getProducts()).hasSize(2);
        assertThat(response.getReturnedCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should return all products when showDisabled is true")
    void shouldReturnAllProductsWhenShowDisabled() {
        ListProductsQuery query = new ListProductsQuery(null, 10, "id", "asc", null, null, true);
        List<Product> products = List.of(buildProduct(1L, "Product A"));
        Window<Product> window = Window.from(products, ScrollPosition::offset);

        when(productRepository.findBy(any(ScrollPosition.class), eq(10), any(Sort.class)))
                .thenReturn(window);

        PagedProductResponse response = useCase.execute(query);

        assertThat(response).isNotNull();
        assertThat(response.getProducts()).hasSize(1);
    }

    @Test
    @DisplayName("Should throw when category does not exist")
    void shouldThrowWhenCategoryNotFound() {
        ListProductsQuery query = new ListProductsQuery(null, 10, "id", "asc", 999L, null, false);

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(query))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    @DisplayName("Should filter by category when categoryId is provided")
    void shouldFilterByCategory() {
        Long categoryId = 5L;
        ListProductsQuery query = new ListProductsQuery(null, 10, "id", "asc", categoryId, null, false);
        List<Product> products = List.of(buildProduct(1L, "Category Product"));
        Window<Product> window = Window.from(products, ScrollPosition::offset);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mock(Category.class)));
        when(productRepository.findByCategoryIdAndIsEnabled(eq(categoryId), eq(true), any(ScrollPosition.class), eq(10), any(Sort.class)))
                .thenReturn(window);

        PagedProductResponse response = useCase.execute(query);

        assertThat(response).isNotNull();
        assertThat(response.getProducts()).hasSize(1);
    }

    @Test
    @DisplayName("Should return empty result when no products found")
    void shouldReturnEmptyWhenNoProducts() {
        ListProductsQuery query = new ListProductsQuery(null, 10, "id", "asc", null, null, false);
        Window<Product> emptyWindow = Window.from(List.of(), ScrollPosition::offset);

        when(productRepository.findByIsEnabled(eq(true), any(ScrollPosition.class), eq(10), any(Sort.class)))
                .thenReturn(emptyWindow);

        PagedProductResponse response = useCase.execute(query);

        assertThat(response).isNotNull();
        assertThat(response.getProducts()).isEmpty();
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getReturnedCount()).isEqualTo(0);
    }
}
