package com.wikiaim.backend.pages;

import jakarta.inject.Singleton;

@Singleton
public class PageMapper {

    public PageResponseDTO toDTO(Page page) {
        if (page == null) {
            return null;
        }

        return new PageResponseDTO(
            page.getId(),
            page.getTitle(),
            page.getSlug(),
            page.getCurrentContent(),
            page.getAuthor() != null ? page.getAuthor().getId() : null,
            page.getUpdatedAt()
        );
    }
}
