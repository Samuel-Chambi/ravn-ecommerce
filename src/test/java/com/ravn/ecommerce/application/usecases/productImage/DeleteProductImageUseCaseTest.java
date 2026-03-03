package com.ravn.ecommerce.application.usecases.productImage;

import com.ravn.ecommerce.application.repositories.ProductImageRepository;
import com.ravn.ecommerce.application.services.ImageStorageService;
import com.ravn.ecommerce.application.usecases.productImage.commands.DeleteProductImageCommand;
import com.ravn.ecommerce.domain.exceptions.EntityNotFoundException;
import com.ravn.ecommerce.domain.model.product.ProductImage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteProductImageUseCaseTest {

    @Mock
    private ProductImageRepository productImageRepository;
    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private DeleteProductImageUseCase useCase;

    private static final Long PRODUCT_ID = 10L;
    private static final Long IMAGE_ID = 1L;

    private ProductImage buildImage(boolean isPrimary) {
        return ProductImage.builder()
                .id(IMAGE_ID)
                .productId(PRODUCT_ID)
                .imageUrl("/images/test.jpg")
                .isPrimary(isPrimary)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should delete non-primary image successfully")
    void shouldDeleteNonPrimaryImageSuccessfully() throws IOException {
        ProductImage image = buildImage(false);
        DeleteProductImageCommand command = new DeleteProductImageCommand(PRODUCT_ID, IMAGE_ID);

        when(productImageRepository.findById(IMAGE_ID)).thenReturn(Optional.of(image));

        useCase.execute(command);

        verify(imageStorageService).delete("/images/test.jpg");
        verify(productImageRepository).deleteById(IMAGE_ID);
    }

    @Test
    @DisplayName("Should promote next image when deleting primary image")
    void shouldPromoteNextImageWhenDeletingPrimary() throws IOException {
        ProductImage primaryImage = buildImage(true);
        ProductImage nextImage = ProductImage.builder()
                .id(2L).productId(PRODUCT_ID).imageUrl("/images/next.jpg")
                .isPrimary(false).createdAt(LocalDateTime.now())
                .build();
        DeleteProductImageCommand command = new DeleteProductImageCommand(PRODUCT_ID, IMAGE_ID);

        when(productImageRepository.findById(IMAGE_ID)).thenReturn(Optional.of(primaryImage));
        when(productImageRepository.findByProductId(PRODUCT_ID)).thenReturn(List.of(nextImage));

        useCase.execute(command);

        verify(productImageRepository).deleteById(IMAGE_ID);
        verify(productImageRepository).save(nextImage);
    }

    @Test
    @DisplayName("Should throw when image does not exist")
    void shouldThrowWhenImageNotFound() {
        DeleteProductImageCommand command = new DeleteProductImageCommand(PRODUCT_ID, IMAGE_ID);

        when(productImageRepository.findById(IMAGE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw when image belongs to different product")
    void shouldThrowWhenImageBelongsToDifferentProduct() {
        ProductImage image = ProductImage.builder()
                .id(IMAGE_ID).productId(999L).imageUrl("/images/test.jpg")
                .isPrimary(false).createdAt(LocalDateTime.now())
                .build();
        DeleteProductImageCommand command = new DeleteProductImageCommand(PRODUCT_ID, IMAGE_ID);

        when(productImageRepository.findById(IMAGE_ID)).thenReturn(Optional.of(image));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Should continue deletion even if storage delete fails")
    void shouldContinueDeletionEvenIfStorageFails() throws IOException {
        ProductImage image = buildImage(false);
        DeleteProductImageCommand command = new DeleteProductImageCommand(PRODUCT_ID, IMAGE_ID);

        when(productImageRepository.findById(IMAGE_ID)).thenReturn(Optional.of(image));
        doThrow(new IOException("Storage error")).when(imageStorageService).delete(any());

        useCase.execute(command);

        verify(productImageRepository).deleteById(IMAGE_ID);
    }
}
