package com.wikiaim.backend.pages;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor
public class PageService {

    private final PageRepository pageRepository;
    private final PageMapper pageMapper;

    public List<PageResponseDTO> getAllPublishedPages() {
        return pageRepository.findByIsPublishedTrue()
                             .stream()
                             .map(pageMapper::toDTO)
                             .toList();
    }

    public Optional<PageResponseDTO> getPageBySlug(String slug) {
        return pageRepository.findBySlugAndIsPublishedTrue(slug)
                             .map(pageMapper::toDTO);
    }
}
