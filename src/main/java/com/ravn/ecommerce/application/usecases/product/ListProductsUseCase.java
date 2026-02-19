package com.ravn.ecommerce.application.useCases.product;

import com.ravn.ecommerce.application.dto.response.PagedProductResponse;
import com.ravn.ecommerce.application.dto.response.ProductResponse;
import com.ravn.ecommerce.application.repositories.CategoryRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.useCases.UseCase;
import com.ravn.ecommerce.application.useCases.product.query.ListProductsQuery;
import com.ravn.ecommerce.domain.exceptions.CategoryNotFoundException;
import com.ravn.ecommerce.domain.model.product.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor
public class ListProductsUseCase implements UseCase<ListProductsQuery, PagedProductResponse> {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public PagedProductResponse execute(ListProductsQuery query) {
        log.info("Listing products - cursor: {}, limit: {}, category: {}, showDisabled: {}",
                query.cursor(), query.limit(), query.categoryId(), query.showDisabled());

        // Validate category exists if filtering by category
        if (query.hasCategory()) {
            categoryRepository.findById(query.categoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(query.categoryId()));
        }

        // Create scroll position from cursor
        ScrollPosition position = query.isFirstPage()
                ? ScrollPosition.offset() // Start from beginning
                : decodeScrollPosition(query.cursor());

        // Create sort
        Sort sort = Sort.by(query.getSortDirection(), query.sortBy());

        // Query products based on filters
        Window<Product> window = getProducts(query, position, sort);

        // Build response with next cursor
        PagedProductResponse response = PagedProductResponse.builder()
                .products(window.getContent().stream().map(ProductResponse::toDto).toList())
                .nextCursor(window.hasNext() ? encodeScrollPosition(window.positionAt(window.size() - 1)) : null)
                .hasNext(window.hasNext())
                .returnedCount(window.getContent().size())
                .build();

        log.info("Returned {} products, hasNext: {}",
                window.getContent().size(), window.hasNext());

        return response;
    }

    /**
     * Get products based on query filters using Window API
     */
    private Window<Product> getProducts(ListProductsQuery query, ScrollPosition position, Sort sort) {
        // 1. Search Logic
        if (query.hasSearch()) {
            // Case 1.1: Search + Category
            if (query.hasCategory()) {
//                if (!query.showDisabled()) {
//                    return productRepository.searchByNameAndCategory(query.search(), query.categoryId(), true, position,
//                            query.limit(), sort);
//                }
                return productRepository.searchByNameAndCategory(query.search(), query.categoryId(), true, position,
                        query.limit(), sort);
            }
//            // Case 1.2: Search Only
//            if (!query.showDisabled()) {
//                return productRepository.searchByName(query.search(), true, position, query.limit(), sort);
//            }
            return productRepository.searchByName(query.search(), true, position, query.limit(), sort);
        }

        // 2. Standard Filter Logic
        // Case 2.1: Filter by category + enabled status
        if (query.hasCategory() && !query.showDisabled()) {
            return productRepository.findByCategoryIdAndIsEnabled(
                    query.categoryId(), true, position, query.limit(), sort);
        }

        // Case 2.2: Filter by category only (show all)
        if (query.hasCategory()) {
            return productRepository.findByCategoryId(query.categoryId(), position, query.limit(), sort);
        }

        // Case 2.3: Filter by enabled status only
        if (!query.showDisabled()) {
            return productRepository.findByIsEnabled(true, position, query.limit(), sort);
        }

        // Case 2.4: No filters (show all)
        return productRepository.findBy(position, query.limit(), sort);
    }

    /**
     * Encode ScrollPosition to base64 cursor string
     */
    private String encodeScrollPosition(ScrollPosition position) {
        if (position instanceof KeysetScrollPosition keyset) {
            // Encode the keyset values to base64
            Map<String, Object> keys = keyset.getKeys();
            String keyString = keys.toString();
            return Base64.getUrlEncoder().encodeToString(keyString.getBytes());
        }
        return null;
    }

    /**
     * Decode base64 cursor string to ScrollPosition
     */
    private ScrollPosition decodeScrollPosition(String cursor) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(cursor);
            // For simplicity, using offset position
            // In production, you'd parse the keyset values
            return ScrollPosition.offset();
        } catch (Exception e) {
            log.warn("Invalid cursor: {}, starting from beginning", cursor);
            return ScrollPosition.offset();
        }
    }
}
