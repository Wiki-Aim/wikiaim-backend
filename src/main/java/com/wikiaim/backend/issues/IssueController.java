package com.wikiaim.backend.issues;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import java.util.List;

@Controller("/api/v1/issues")
@Secured(SecurityRule.IS_ANONYMOUS) // TODO : passer à IS_AUTHENTICATED quand le JWT sera en place
@Tag(name = "Issues", description = "Signalement de problèmes et suggestions")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @Post
    @Operation(summary = "Créer une issue", description = "Crée un nouveau signalement de problème ou une suggestion")
    @ApiResponse(responseCode = "201", description = "Issue créée")
    public HttpResponse<IssueResponseDTO> createIssue(@Body @Valid CreateIssueDTO dto) {
        return HttpResponse.created(issueService.createIssue(dto));
    }

    @Get("/open")
    @Operation(summary = "Lister les issues ouvertes", description = "Retourne toutes les issues au statut OPEN")
    public List<IssueResponseDTO> listOpenIssues() {
        return issueService.getOpenIssues();
    }
}
