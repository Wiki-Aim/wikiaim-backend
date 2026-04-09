package com.wikiaim.backend.pages;

import com.wikiaim.backend.categories.CategoryRepository;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor
public class PageService {

    private final PageRepository pageRepository;
    private final PageMapper pageMapper;
    private final CategoryRepository categoryRepository;

    public Optional<PageResponseDTO> getPageBySlug(String slug) {
        return pageRepository.findBySlugAndIsPublishedTrue(slug)
                             .map(pageMapper::toDTO);
    }

    public Optional<List<PageSummaryDTO>> getPagesByCategorySlug(String categorySlug) {
        return categoryRepository.findBySlug(categorySlug)
                .map(category -> pageRepository.findByCategoryAndIsPublishedTrue(category)
                        .stream()
                        .map(pageMapper::toSummaryDTO)
                        .toList());
    }

    public Optional<List<PageSummaryDTO>> searchPagesByCategorySlug(String categorySlug, String query) {
        return categoryRepository.findBySlug(categorySlug)
                .map(category -> pageRepository.findByCategoryAndIsPublishedTrueAndTitleContainsIgnoreCase(category, query)
                        .stream()
                        .map(pageMapper::toSummaryDTO)
                        .toList());
    }
}
