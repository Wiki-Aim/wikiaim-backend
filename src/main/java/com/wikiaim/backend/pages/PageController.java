package com.wikiaim.backend.pages;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.micronaut.validation.Validated;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Controller("/api/v1/pages")
@Secured(SecurityRule.IS_ANONYMOUS)
@Validated
@RequiredArgsConstructor
@Tag(name = "Pages", description = "Gestion des pages du wiki")
public class PageController {

    private final PageService pageService;

    @Get("/{slug}")
    @Operation(summary = "Récupérer une page par son slug", description = "Retourne une page publiée correspondant au slug donné")
    @ApiResponse(responseCode = "200", description = "Page trouvée")
    @ApiResponse(responseCode = "404", description = "Aucune page publiée avec ce slug")
    public HttpResponse<PageResponseDTO> getPage(@PathVariable String slug) {
        return pageService.getPageBySlug(slug)
                          .map(HttpResponse::ok)
                          .orElse(HttpResponse.notFound());
    }

    @Get("/category/{categorySlug}")
    @Operation(summary = "Lister les pages d'une catégorie", description = "Retourne toutes les pages publiées appartenant à la catégorie donnée")
    @ApiResponse(responseCode = "200", description = "Pages trouvées")
    @ApiResponse(responseCode = "404", description = "Catégorie introuvable")
    public HttpResponse<List<PageSummaryDTO>> listPagesByCategory(@PathVariable String categorySlug) {
        return pageService.getPagesByCategorySlug(categorySlug)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Get("/category/{categorySlug}/search{?q}")
    @Operation(summary = "Rechercher des pages dans une catégorie", description = "Recherche par titre parmi les pages publiées d'une catégorie")
    @ApiResponse(responseCode = "200", description = "Résultats de recherche")
    @ApiResponse(responseCode = "404", description = "Catégorie introuvable")
    public HttpResponse<List<PageSummaryDTO>> searchPagesInCategory(@PathVariable String categorySlug, @QueryValue @Nullable String q) {
        return pageService.searchPagesByCategorySlug(categorySlug, q == null ? "" : q)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }
}
