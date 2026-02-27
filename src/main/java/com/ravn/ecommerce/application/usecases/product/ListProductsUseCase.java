package com.ravn.ecommerce.application.usecases.product;

import com.ravn.ecommerce.application.dto.response.PagedProductResponse;
import com.ravn.ecommerce.application.dto.response.ProductResponse;
import com.ravn.ecommerce.application.repositories.CategoryRepository;
import com.ravn.ecommerce.application.repositories.ProductRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.usecases.product.query.ListProductsQuery;
import com.ravn.ecommerce.application.utils.CursorUtils;
import com.ravn.ecommerce.domain.exceptions.CategoryNotFoundException;
import com.ravn.ecommerce.domain.model.product.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Service;

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
                ? ScrollPosition.offset()
                : CursorUtils.decode(query.cursor());

        // Create sort
        Sort sort = Sort.by(query.getSortDirection(), query.sortBy());

        // Query products based on filters
        Window<Product> window = getProducts(query, position, sort);

        // Build response with next cursor
        PagedProductResponse response = PagedProductResponse.builder()
                .products(window.getContent().stream().map(ProductResponse::toDto).toList())
                .nextCursor(window.hasNext() ? CursorUtils.encode(window.positionAt(window.size() - 1)) : null)
                .hasNext(window.hasNext())
                .returnedCount(window.getContent().size())
                .build();

        log.info("Returned {} products, hasNext: {}",
                window.getContent().size(), window.hasNext());

        return response;
    }

    /**
     * Get products based on query filters using Window API.
     * Handles all combinations of search, category, and enabled filters.
     */
    private Window<Product> getProducts(ListProductsQuery query, ScrollPosition position, Sort sort) {
        boolean hasSearch = query.hasSearch();
        boolean hasCategory = query.hasCategory();
        boolean onlyEnabled = !query.showDisabled();

        // Search + Category + only enabled
        if (hasSearch && hasCategory && onlyEnabled) {
            return productRepository.searchByNameAndCategory(query.search(), query.categoryId(), true, position,
                    query.limit(), sort);
        }
        // Search + Category + show all
        if (hasSearch && hasCategory) {
            return productRepository.searchByNameAndCategory(query.search(), query.categoryId(), position,
                    query.limit(), sort);
        }
        // Search + only enabled
        if (hasSearch && onlyEnabled) {
            return productRepository.searchByName(query.search(), true, position, query.limit(), sort);
        }
        // Search + show all
        if (hasSearch) {
            return productRepository.searchByName(query.search(), position, query.limit(), sort);
        }
        // Category + only enabled
        if (hasCategory && onlyEnabled) {
            return productRepository.findByCategoryIdAndIsEnabled(query.categoryId(), true, position, query.limit(),
                    sort);
        }
        // Category + show all
        if (hasCategory) {
            return productRepository.findByCategoryId(query.categoryId(), position, query.limit(), sort);
        }
        // Only enabled
        if (onlyEnabled) {
            return productRepository.findByIsEnabled(true, position, query.limit(), sort);
        }
        // No filters (show all)
        return productRepository.findBy(position, query.limit(), sort);
    }

}
