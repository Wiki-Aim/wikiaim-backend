package com.wikiaim.backend.pages;

import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Singleton
public class PageService {

    private final PageRepository pageRepository;
    private final PageMapper pageMapper;

    public PageService(PageRepository pageRepository, PageMapper pageMapper) {
        this.pageRepository = pageRepository;
        this.pageMapper = pageMapper;
    }

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
