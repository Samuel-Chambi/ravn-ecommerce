package com.ravn.ecommerce.application.usecases.productImage;

import com.ravn.ecommerce.application.dto.response.ProductImageResponse;
import com.ravn.ecommerce.application.repositories.ProductImageRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.services.ImageStorageService;
import com.ravn.ecommerce.application.usecases.productImage.commands.UploadProductImageCommand;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.exceptions.InvalidOperationException;
import com.ravn.ecommerce.domain.model.product.Product;
import com.ravn.ecommerce.domain.model.product.ProductImage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadProductImageUseCaseTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductImageRepository productImageRepository;
    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private UploadProductImageUseCase useCase;

    private static final Long PRODUCT_ID = 10L;

    private Product buildProduct() {
        return Product.builder()
                .id(PRODUCT_ID)
                .name("Test Product")
                .price(new BigDecimal("25.00"))
                .isEnabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should upload image successfully")
    void shouldUploadImageSuccessfully() throws IOException {
        Product product = buildProduct();
        MultipartFile file = mock(MultipartFile.class);
        UploadProductImageCommand command = new UploadProductImageCommand(PRODUCT_ID, List.of(file), true);

        ProductImage savedImage = ProductImage.builder()
                .id(1L).productId(PRODUCT_ID).imageUrl("/images/test.jpg")
                .isPrimary(true).createdAt(LocalDateTime.now())
                .build();

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productImageRepository.countByProductId(PRODUCT_ID)).thenReturn(0);
        when(imageStorageService.store(any(MultipartFile.class), eq(PRODUCT_ID))).thenReturn("/images/test.jpg");
        when(productImageRepository.save(any(ProductImage.class))).thenReturn(savedImage);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        List<ProductImageResponse> responses = useCase.execute(command);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getImageUrl()).isEqualTo("/images/test.jpg");
    }

    @Test
    @DisplayName("Should throw when product does not exist")
    void shouldThrowWhenProductNotFound() {
        MultipartFile file = mock(MultipartFile.class);
        UploadProductImageCommand command = new UploadProductImageCommand(PRODUCT_ID, List.of(file), true);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw when exceeding max images limit")
    void shouldThrowWhenExceedingMaxImages() {
        Product product = buildProduct();
        MultipartFile file = mock(MultipartFile.class);
        UploadProductImageCommand command = new UploadProductImageCommand(PRODUCT_ID, List.of(file), false);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productImageRepository.countByProductId(PRODUCT_ID)).thenReturn(5);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Cannot upload");
    }

    @Test
    @DisplayName("Should throw when image storage fails")
    void shouldThrowWhenStorageFails() throws IOException {
        Product product = buildProduct();
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        UploadProductImageCommand command = new UploadProductImageCommand(PRODUCT_ID, List.of(file), false);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productImageRepository.countByProductId(PRODUCT_ID)).thenReturn(0);
        when(imageStorageService.store(any(MultipartFile.class), eq(PRODUCT_ID)))
                .thenThrow(new IOException("Storage error"));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Failed to upload");
    }
}
