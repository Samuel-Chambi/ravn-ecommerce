package com.ravn.ecommerce.application.usecases.product;

import com.ravn.ecommerce.application.dto.request.product.ProductRequest;
import com.ravn.ecommerce.application.dto.response.ProductResponse;
import com.ravn.ecommerce.application.repositories.CategoryRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.product.command.CreateProductCommand;
import com.ravn.ecommerce.domain.exceptions.CategoryNotFoundException;
import com.ravn.ecommerce.domain.exceptions.UnauthorizedException;
import com.ravn.ecommerce.domain.model.product.Product;
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
class CreateProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CreateProductUseCase useCase;

    private static final Long USER_ID = 1L;
    private static final Long CATEGORY_ID = 5L;

    private User buildUser(UserRole role) {
        return new User(USER_ID, "manager@test.com", "hashedpass", role, true, LocalDateTime.now());
    }

    private ProductRequest buildProductRequest() {
        return ProductRequest.builder()
                .name("Test Product")
                .description("A great test product for testing")
                .price(new BigDecimal("49.99"))
                .stock(100)
                .categoryId(CATEGORY_ID)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("Should create product successfully when user is MANAGER")
    void shouldCreateProductSuccessfully() {
        User manager = buildUser(UserRole.MANAGER);
        ProductRequest request = buildProductRequest();
        CreateProductCommand command = new CreateProductCommand(request, USER_ID);

        Product savedProduct = Product.builder()
                .id(1L)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .isEnabled(true)
                .categoryId(CATEGORY_ID)
                .createdBy(USER_ID)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(manager));
        when(categoryRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.of(mock(com.ravn.ecommerce.domain.model.product.Category.class)));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductResponse response = useCase.execute(command);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Test Product");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw when user does not exist")
    void shouldThrowWhenUserNotFound() {
        CreateProductCommand command = new CreateProductCommand(buildProductRequest(), USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Should throw when user is CLIENT (not MANAGER)")
    void shouldThrowWhenUserNotManager() {
        User client = buildUser(UserRole.CLIENT);
        CreateProductCommand command = new CreateProductCommand(buildProductRequest(), USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("MANAGER");
    }

    @Test
    @DisplayName("Should throw when category does not exist")
    void shouldThrowWhenCategoryNotFound() {
        User manager = buildUser(UserRole.MANAGER);
        CreateProductCommand command = new CreateProductCommand(buildProductRequest(), USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(manager));
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(CategoryNotFoundException.class);
    }
}
