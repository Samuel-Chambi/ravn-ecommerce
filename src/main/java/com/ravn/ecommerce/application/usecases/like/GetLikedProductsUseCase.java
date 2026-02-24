package com.ravn.ecommerce.application.usecases.like;

import com.ravn.ecommerce.application.dto.response.ProductResponse;
import com.ravn.ecommerce.application.usecases.like.command.GetLikedProductsCommand;
import org.springframework.data.domain.Window;
import com.ravn.ecommerce.application.repositories.LikeRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.product.Like;
import com.ravn.ecommerce.domain.model.product.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetLikedProductsUseCase implements UseCase<GetLikedProductsCommand, Window<ProductResponse>> {

    private final LikeRepository likeRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Window<ProductResponse> execute(GetLikedProductsCommand command) {
        log.info("Fetching liked products with cursor pagination for user ID: {}", command.userId());

        if (!userRepository.existsById(command.userId())) {
            throw new UserNotFound(String.format("User ID %d does not exist", command.userId()));
        }

        Window<Like> likesWindow = likeRepository.findAllByUserId(
                command.userId(),
                command.position(),
                command.limit());

        if (likesWindow.isEmpty()) {
            return Window.from(List.of(), (val) -> null);
        }

        List<Long> productIds = likesWindow.getContent().stream()
                .map(Like::getProductId)
                .collect(Collectors.toList());

        List<Product> products = productRepository.findAllById(productIds);

        // Map Likes to ProductResponse preserving the Window characteristics
        return likesWindow.map(like -> {
            Product product = products.stream()
                    .filter(p -> p.getId().equals(like.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Product missing for like"));
            return ProductResponse.toDto(product);
        });
    }
}
