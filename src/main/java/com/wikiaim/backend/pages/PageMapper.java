package com.wikiaim.backend.pages;

import io.micronaut.context.annotation.Mapper;
import io.micronaut.context.annotation.Mapper.Mapping;
import jakarta.inject.Singleton;

@Singleton
public interface PageMapper {

    @Mapper({
        @Mapping(to = "authorId", from = "#{page.author.id}", condition = "#{page.author != null}"),
        @Mapping(to = "categorySlug", from = "#{page.category.slug}", condition = "#{page.category != null}")
    })
    PageResponseDTO toDTO(Page page);

    @Mapper({
        @Mapping(to = "authorId", from = "#{page.author.id}", condition = "#{page.author != null}"),
        @Mapping(to = "categorySlug", from = "#{page.category.slug}", condition = "#{page.category != null}")
    })
    PageSummaryDTO toSummaryDTO(Page page);
}
