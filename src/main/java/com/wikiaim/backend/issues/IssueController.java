package com.wikiaim.backend.issues;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.micronaut.validation.Validated;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Controller("/api/v1/issues")
@Validated
@RequiredArgsConstructor
@Tag(name = "Issues", description = "Signalement de problèmes et suggestions")
public class IssueController {

    private final IssueService issueService;

    @Post
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Créer une issue", description = "Crée un nouveau signalement de problème ou une suggestion")
    @ApiResponse(responseCode = "201", description = "Issue créée")
    public HttpResponse<IssueResponseDTO> createIssue(@Body @Valid CreateIssueDTO dto) {
        return HttpResponse.created(issueService.createIssue(dto));
    }

    @Get("/open")
    @Secured(SecurityRule.IS_ANONYMOUS)
    @Operation(summary = "Lister les issues ouvertes", description = "Retourne toutes les issues au statut OPEN")
    public List<IssueResponseDTO> listOpenIssues() {
        return issueService.getOpenIssues();
    }

    @Patch("/{id}/status")
    @Secured({"MODERATOR", "ADMIN"})
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Changer le statut d'une issue", description = "Met à jour le statut d'une issue existante")
    @ApiResponse(responseCode = "200", description = "Statut mis à jour")
    @ApiResponse(responseCode = "400", description = "Statut invalide ou identique au statut actuel")
    @ApiResponse(responseCode = "403", description = "Accès refusé — rôle MODERATOR ou ADMIN requis")
    @ApiResponse(responseCode = "404", description = "Issue introuvable")
    public HttpResponse<IssueResponseDTO> updateStatus(@PathVariable UUID id, @Body @Valid UpdateIssueStatusDTO dto) {
        return HttpResponse.ok(issueService.updateStatus(id, dto));
    }
}
