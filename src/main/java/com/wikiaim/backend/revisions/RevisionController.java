package com.wikiaim.backend.revisions;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller("/api/v1/revisions")
public class RevisionController {

    private final RevisionService revisionService;

    public RevisionController(RevisionService revisionService) {
        this.revisionService = revisionService;
    }

    @Post
    public HttpResponse<RevisionResponseDTO> proposeRevision(@Body ProposeRevisionDTO dto) {
        RevisionResponseDTO createdRevision = revisionService.proposeRevision(dto);
        return HttpResponse.created(createdRevision);
    }

    @Get("/pending")
    public List<RevisionResponseDTO> listPending() {
        return revisionService.getPendingRevisions();
    }

    // TODO : extraire automatiquement du token JWT le reviewerId.
    @Post("/{id}/approve")
    public HttpResponse<?> approveRevision(@PathVariable UUID id, @QueryValue UUID reviewerId) {
        try {
            revisionService.approveRevision(id, reviewerId);
            return HttpResponse.ok();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return HttpResponse.badRequest(e.getMessage());
        }
    }
}