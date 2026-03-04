package com.wikiaim.backend.pages;

import io.micronaut.context.annotation.Mapper;
import io.micronaut.context.annotation.Mapper.Mapping;
import jakarta.inject.Singleton;

@Singleton
public interface PageMapper {

    @Mapping(to = "authorId", from = "#{page.author.id}", condition = "#{page.author != null}")
    @Mapper
    PageResponseDTO toDTO(Page page);
}
