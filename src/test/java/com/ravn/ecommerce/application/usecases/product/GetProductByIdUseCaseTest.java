package com.ravn.ecommerce.application.usecases.product;

import com.ravn.ecommerce.application.dto.response.ProductResponse;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.domain.exceptions.ProductNotFound;
import com.ravn.ecommerce.domain.model.product.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProductByIdUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private GetProductByIdUseCase useCase;

    private static final Long PRODUCT_ID = 10L;

    private Product buildProduct(boolean enabled) {
        return Product.builder()
                .id(PRODUCT_ID)
                .name("Test Product")
                .description("A test product")
                .price(new BigDecimal("49.99"))
                .isEnabled(enabled)
                .categoryId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should return product when it exists and is enabled")
    void shouldReturnProductWhenExistsAndEnabled() {
        Product product = buildProduct(true);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        ProductResponse response = useCase.execute(PRODUCT_ID);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(PRODUCT_ID);
        assertThat(response.getName()).isEqualTo("Test Product");
    }

    @Test
    @DisplayName("Should throw when product does not exist")
    void shouldThrowWhenProductNotFound() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(PRODUCT_ID))
                .isInstanceOf(ProductNotFound.class)
                .hasMessageContaining("Product ID " + PRODUCT_ID);
    }

    @Test
    @DisplayName("Should throw when product exists but is disabled")
    void shouldThrowWhenProductIsDisabled() {
        Product disabledProduct = buildProduct(false);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(disabledProduct));

        assertThatThrownBy(() -> useCase.execute(PRODUCT_ID))
                .isInstanceOf(ProductNotFound.class)
                .hasMessageContaining("Product ID " + PRODUCT_ID);
    }
}
