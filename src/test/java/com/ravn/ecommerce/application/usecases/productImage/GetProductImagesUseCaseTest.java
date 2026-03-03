package com.ravn.ecommerce.application.usecases.productImage;

import com.ravn.ecommerce.application.dto.response.ProductImageResponse;
import com.ravn.ecommerce.application.repositories.ProductImageRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.model.product.Product;
import com.ravn.ecommerce.domain.model.product.ProductImage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProductImagesUseCaseTest {

    @Mock
    private ProductImageRepository productImageRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private GetProductImagesUseCase useCase;

    private static final Long PRODUCT_ID = 10L;

    @Test
    @DisplayName("Should return images for product")
    void shouldReturnProductImages() {
        Product product = Product.builder().id(PRODUCT_ID).name("Test").price(new BigDecimal("25.00")).build();
        List<ProductImage> images = List.of(
                ProductImage.builder().id(1L).productId(PRODUCT_ID).imageUrl("/img1.jpg").isPrimary(true).createdAt(LocalDateTime.now()).build(),
                ProductImage.builder().id(2L).productId(PRODUCT_ID).imageUrl("/img2.jpg").isPrimary(false).createdAt(LocalDateTime.now()).build()
        );

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productImageRepository.findByProductId(PRODUCT_ID)).thenReturn(images);

        List<ProductImageResponse> responses = useCase.execute(PRODUCT_ID);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getIsPrimary()).isTrue();
    }

    @Test
    @DisplayName("Should return empty list when product has no images")
    void shouldReturnEmptyWhenNoImages() {
        Product product = Product.builder().id(PRODUCT_ID).name("Test").price(new BigDecimal("25.00")).build();

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productImageRepository.findByProductId(PRODUCT_ID)).thenReturn(List.of());

        List<ProductImageResponse> responses = useCase.execute(PRODUCT_ID);

        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("Should throw when product does not exist")
    void shouldThrowWhenProductNotFound() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(PRODUCT_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
