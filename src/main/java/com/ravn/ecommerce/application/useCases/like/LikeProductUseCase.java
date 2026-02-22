package com.ravn.ecommerce.application.useCases.like;

import com.ravn.ecommerce.application.dto.response.PagedProductResponse;
import com.ravn.ecommerce.application.dto.response.ProductResponse;
import com.ravn.ecommerce.application.repositories.LikeRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.repositories.UserRepository;
import com.ravn.ecommerce.application.useCases.UseCase;
import com.ravn.ecommerce.application.useCases.like.command.SwitchLikeProductCommand;
import com.ravn.ecommerce.domain.exceptions.ProductNotFound;
import com.ravn.ecommerce.domain.exceptions.UserNotFound;
import com.ravn.ecommerce.domain.model.product.Like;
import com.ravn.ecommerce.domain.model.product.Product;
import com.ravn.ecommerce.domain.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class LikeProductUseCase implements UseCase<SwitchLikeProductCommand, Void> {
    private final ProductRepository productRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Void execute(SwitchLikeProductCommand command) {
        Long userId = command.userId();
        Long productId = command.productId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound(String.format("User ID %d does not exist", userId)));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFound(String.format("Product ID %d does not exist", productId)));
        Like like = Like.builder()
                .userId(userId)
                .productId(productId)
                .createdAt(LocalDateTime.now())
                .build();
        likeRepository.save(like);
        return null;
    }

}
