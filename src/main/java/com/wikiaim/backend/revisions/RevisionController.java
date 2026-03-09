package com.wikiaim.backend.revisions;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller("/api/v1/revisions")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Validated
@RequiredArgsConstructor
@Tag(name = "Revisions", description = "Proposition et modération des révisions de pages")
@SecurityRequirement(name = "BearerAuth")
public class RevisionController {

    private final RevisionService revisionService;

    @Post
    @Operation(summary = "Proposer une révision", description = "Crée une nouvelle proposition de modification pour une page existante")
    @ApiResponse(responseCode = "201", description = "Révision créée")
    public HttpResponse<RevisionResponseDTO> proposeRevision(@Body @Valid ProposeRevisionDTO dto) {
        RevisionResponseDTO createdRevision = revisionService.proposeRevision(dto);
        return HttpResponse.created(createdRevision);
    }

    @Get("/pending")
    @Operation(summary = "Lister les révisions en attente", description = "Retourne toutes les révisions au statut PENDING")
    public List<RevisionResponseDTO> listPending() {
        return revisionService.getPendingRevisions();
    }

    @Get("/{id}/diff")
    @Operation(summary = "Voir le diff d'une révision", description = "Calcule et retourne le diff entre le contenu actuel de la page et le contenu proposé par la révision")
    @ApiResponse(responseCode = "200", description = "Diff calculé")
    @ApiResponse(responseCode = "400", description = "Révision introuvable")
    public RevisionDiffDTO getRevisionDiff(@PathVariable UUID id) {
        return revisionService.getRevisionDiff(id);
    }

    @Post("/{id}/approve")
    @Secured({"MODERATOR", "ADMIN"})
    @Operation(summary = "Approuver une révision", description = "Valide une révision PENDING et met à jour le contenu de la page. Le reviewer est identifié via le token JWT.")
    @ApiResponse(responseCode = "200", description = "Révision approuvée, page mise à jour")
    @ApiResponse(responseCode = "400", description = "Révision introuvable, déjà traitée, ou modérateur introuvable")
    @ApiResponse(responseCode = "403", description = "Accès refusé — rôle MODERATOR ou ADMIN requis")
    public HttpResponse<Void> approveRevision(@PathVariable UUID id, Principal principal) {
        UUID reviewerId = UUID.fromString(principal.getName());
        revisionService.approveRevision(id, reviewerId);
        return HttpResponse.ok();
    }
}
