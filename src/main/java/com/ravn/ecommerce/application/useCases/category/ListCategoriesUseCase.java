package com.ravn.ecommerce.application.usecases.category;

import com.ravn.ecommerce.application.dto.response.CategoryResponse;
import com.ravn.ecommerce.application.dto.response.PagedCategoryResponse;
import com.ravn.ecommerce.application.repositories.CategoryRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.usecases.category.query.ListCategoriesQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ListCategoriesUseCase implements UseCase<ListCategoriesQuery, PagedCategoryResponse> {
    private final CategoryRepository categoryRepository;

    @Override
    public PagedCategoryResponse execute(ListCategoriesQuery query) {
        String cursor = query.cursor();
        int limit = query.limit();
        log.info("Listing categories - cursor: {} , limit: {}", cursor, limit);

        ScrollPosition position = query.isFirstPage() ? ScrollPosition.offset() : decodeScrollPosition(cursor);

        Window<CategoryResponse> window = categoryRepository.findBy(position, limit)
                .map(CategoryResponse::toDto);
        PagedCategoryResponse response = PagedCategoryResponse.builder()
                .categories(window.getContent())
                .nextCursor(window.hasNext() ? encodeScrollPosition(window.positionAt(window.size() - 1)) : null)
                .hasNext(window.hasNext())
                .returnedCount(window.getContent().size())
                .build();
        log.info("Returned {} categories", window.getContent().size());
        return response;
    }

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
