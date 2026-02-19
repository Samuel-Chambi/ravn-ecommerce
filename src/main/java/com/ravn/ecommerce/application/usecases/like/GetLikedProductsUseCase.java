package com.ravn.ecommerce.application.useCases.like;

import com.ravn.ecommerce.application.dto.response.PagedProductResponse;
import com.ravn.ecommerce.application.dto.response.ProductResponse;
import com.ravn.ecommerce.application.repositories.LikeRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.useCases.UseCase;
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
public class GetLikedProductsUseCase implements UseCase<Long, PagedProductResponse> {

    private final LikeRepository likeRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PagedProductResponse execute(Long userId) {
        log.info("Fetching liked products for user ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new UserNotFound(String.format("User ID %d does not exist", userId));
        }

        List<Like> likes = likeRepository.findAllByUserId(userId);

        if (likes.isEmpty()) {
            return PagedProductResponse.builder()
                    .products(List.of())
                    .returnedCount(0)
                    .hasNext(false)
                    .nextCursor(null)
                    .build();
        }

        List<Long> productIds = likes.stream()
                .map(Like::getProductId)
                .collect(Collectors.toList());

        List<Product> products = productRepository.findAllById(productIds);

        List<ProductResponse> productResponses = products.stream()
                .map(ProductResponse::toDto)
                .collect(Collectors.toList());

        return PagedProductResponse.builder()
                .products(productResponses)
                .returnedCount(productResponses.size())
                .hasNext(false)
                .nextCursor(null)
                .build();
    }
}
