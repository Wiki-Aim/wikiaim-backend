package com.wikiaim.backend.categories;

import io.micronaut.context.annotation.Mapper;
import jakarta.inject.Singleton;

@Singleton
public interface CategoryMapper {

    @Mapper
    CategoryResponseDTO toDTO(Category category);
}
