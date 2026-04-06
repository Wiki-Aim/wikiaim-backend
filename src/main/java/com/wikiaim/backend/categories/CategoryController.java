package com.wikiaim.backend.categories;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Controller("/api/v1/categories")
@Secured(SecurityRule.IS_ANONYMOUS)
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Liste des catégories du wiki")
public class CategoryController {

    private final CategoryService categoryService;

    @Get
    @Operation(summary = "Lister les catégories", description = "Retourne toutes les catégories disponibles")
    public List<CategoryResponseDTO> listCategories() {
        return categoryService.getAllCategories();
    }
}
