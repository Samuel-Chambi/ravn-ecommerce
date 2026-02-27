package com.ravn.ecommerce.application.usecases.category;

import com.ravn.ecommerce.application.dto.response.CategoryResponse;
import com.ravn.ecommerce.application.dto.response.PagedCategoryResponse;
import com.ravn.ecommerce.application.repositories.CategoryRepository;
import com.ravn.ecommerce.application.usecases.UseCase;
import com.ravn.ecommerce.application.usecases.category.query.ListCategoriesQuery;
import com.ravn.ecommerce.application.utils.CursorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Service;

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

        ScrollPosition position = query.isFirstPage() ? ScrollPosition.offset() : CursorUtils.decode(cursor);

        Window<CategoryResponse> window = categoryRepository.findBy(position, limit)
                .map(CategoryResponse::toDto);
        PagedCategoryResponse response = PagedCategoryResponse.builder()
                .categories(window.getContent())
                .nextCursor(window.hasNext() ? CursorUtils.encode(window.positionAt(window.size() - 1)) : null)
                .hasNext(window.hasNext())
                .returnedCount(window.getContent().size())
                .build();
        log.info("Returned {} categories", window.getContent().size());
        return response;
    }

}
