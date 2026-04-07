package com.wikiaim.backend.categories;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Singleton
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                            .map(categoryMapper::toDTO)
                            .toList();
    }
}
