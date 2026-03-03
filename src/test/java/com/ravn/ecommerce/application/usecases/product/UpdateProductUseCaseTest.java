package com.ravn.ecommerce.application.usecases.product;

import com.ravn.ecommerce.application.dto.request.product.UpdateProductRequest;
import com.ravn.ecommerce.application.dto.response.ProductResponse;
import com.ravn.ecommerce.application.events.EventPublisher;
import com.ravn.ecommerce.application.repositories.CategoryRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.product.command.UpdateProductCommand;
import com.ravn.ecommerce.domain.exceptions.CategoryNotFoundException;
import com.ravn.ecommerce.domain.exceptions.ProductNotFound;
import com.ravn.ecommerce.domain.exceptions.UnauthorizedException;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.product.Category;
import com.ravn.ecommerce.domain.model.product.Inventory;
import com.ravn.ecommerce.domain.model.product.Product;
import com.ravn.ecommerce.domain.model.product.events.LowStockEvent;
import com.ravn.ecommerce.domain.model.product.events.ProductDiscountedEvent;
import com.ravn.ecommerce.domain.model.user.User;
import com.ravn.ecommerce.domain.model.user.UserRole;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private UpdateProductUseCase useCase;

    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 10L;
    private static final Long CATEGORY_ID = 5L;

    private User buildUser(UserRole role) {
        return User.builder()
                .id(USER_ID)
                .email("manager@test.com")
                .passwordHash("hashed")
                .role(role)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Product buildProduct() {
        return Product.builder()
                .id(PRODUCT_ID)
                .name("Original Product")
                .description("Original description")
                .price(new BigDecimal("100.00"))
                .isEnabled(true)
                .categoryId(CATEGORY_ID)
                .inventory(Inventory.builder()
                        .id(1L)
                        .productId(PRODUCT_ID)
                        .quantity(50)
                        .updatedAt(LocalDateTime.now())
                        .build())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should update product name successfully")
    void shouldUpdateProductNameSuccessfully() {
        User manager = buildUser(UserRole.MANAGER);
        Product product = buildProduct();
        UpdateProductRequest request = UpdateProductRequest.builder().name("Updated Product").build();
        UpdateProductCommand command = new UpdateProductCommand(PRODUCT_ID, request, USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(manager));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = useCase.execute(command);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Updated Product");
    }

    @Test
    @DisplayName("Should publish ProductDiscountedEvent when price is reduced")
    void shouldPublishDiscountEventWhenPriceReduced() {
        User manager = buildUser(UserRole.MANAGER);
        Product product = buildProduct();
        UpdateProductRequest request = UpdateProductRequest.builder().price(new BigDecimal("50.00")).build();
        UpdateProductCommand command = new UpdateProductCommand(PRODUCT_ID, request, USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(manager));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        useCase.execute(command);

        verify(eventPublisher).publish(any(ProductDiscountedEvent.class));
    }

    @Test
    @DisplayName("Should not publish discount event when price increases")
    void shouldNotPublishDiscountEventWhenPriceIncreases() {
        User manager = buildUser(UserRole.MANAGER);
        Product product = buildProduct();
        UpdateProductRequest request = UpdateProductRequest.builder().price(new BigDecimal("200.00")).build();
        UpdateProductCommand command = new UpdateProductCommand(PRODUCT_ID, request, USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(manager));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        useCase.execute(command);

        verify(eventPublisher, never()).publish(any(ProductDiscountedEvent.class));
    }

    @Test
    @DisplayName("Should publish LowStockEvent when stock drops below threshold")
    void shouldPublishLowStockEventWhenStockBelowThreshold() {
        User manager = buildUser(UserRole.MANAGER);
        Product product = buildProduct();
        UpdateProductRequest request = UpdateProductRequest.builder().stock(2).build();
        UpdateProductCommand command = new UpdateProductCommand(PRODUCT_ID, request, USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(manager));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        useCase.execute(command);

        verify(eventPublisher).publish(any(LowStockEvent.class));
    }

    @Test
    @DisplayName("Should throw when user does not exist")
    void shouldThrowWhenUserNotFound() {
        UpdateProductRequest request = UpdateProductRequest.builder().name("Updated").build();
        UpdateProductCommand command = new UpdateProductCommand(PRODUCT_ID, request, USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFound.class);
    }

    @Test
    @DisplayName("Should throw when user is not MANAGER")
    void shouldThrowWhenUserNotManager() {
        User client = buildUser(UserRole.CLIENT);
        UpdateProductRequest request = UpdateProductRequest.builder().name("Updated").build();
        UpdateProductCommand command = new UpdateProductCommand(PRODUCT_ID, request, USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Should throw when product does not exist")
    void shouldThrowWhenProductNotFound() {
        User manager = buildUser(UserRole.MANAGER);
        UpdateProductRequest request = UpdateProductRequest.builder().name("Updated").build();
        UpdateProductCommand command = new UpdateProductCommand(PRODUCT_ID, request, USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(manager));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ProductNotFound.class);
    }

    @Test
    @DisplayName("Should throw when category does not exist")
    void shouldThrowWhenCategoryNotFound() {
        User manager = buildUser(UserRole.MANAGER);
        Product product = buildProduct();
        UpdateProductRequest request = UpdateProductRequest.builder().categoryId(999L).build();
        UpdateProductCommand command = new UpdateProductCommand(PRODUCT_ID, request, USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(manager));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    @DisplayName("Should update category when it exists")
    void shouldUpdateCategorySuccessfully() {
        User manager = buildUser(UserRole.MANAGER);
        Product product = buildProduct();
        Long newCategoryId = 20L;
        UpdateProductRequest request = UpdateProductRequest.builder().categoryId(newCategoryId).build();
        UpdateProductCommand command = new UpdateProductCommand(PRODUCT_ID, request, USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(manager));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(newCategoryId)).thenReturn(Optional.of(mock(Category.class)));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = useCase.execute(command);

        assertThat(response).isNotNull();
        assertThat(product.getCategoryId()).isEqualTo(newCategoryId);
    }
}
