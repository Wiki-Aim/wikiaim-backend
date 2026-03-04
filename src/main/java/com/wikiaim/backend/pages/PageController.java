package com.wikiaim.backend.pages;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.micronaut.validation.Validated;

import java.util.List;

@Controller("/api/v1/pages")
@Secured(SecurityRule.IS_ANONYMOUS)
@Validated
@Tag(name = "Pages", description = "Gestion des pages du wiki")
public class PageController {

    private final PageService pageService;

    public PageController(PageService pageService) {
        this.pageService = pageService;
    }

    @Get
    @Operation(summary = "Lister les pages publiées", description = "Retourne toutes les pages dont le statut est publié")
    public List<PageResponseDTO> listPublishedPages() {
        return pageService.getAllPublishedPages();
    }

    @Get("/{slug}")
    @Operation(summary = "Récupérer une page par son slug", description = "Retourne une page publiée correspondant au slug donné")
    @ApiResponse(responseCode = "200", description = "Page trouvée")
    @ApiResponse(responseCode = "404", description = "Aucune page publiée avec ce slug")
    public HttpResponse<PageResponseDTO> getPage(@PathVariable String slug) {
        return pageService.getPageBySlug(slug)
                          .map(HttpResponse::ok)
                          .orElse(HttpResponse.notFound());
    }
}
