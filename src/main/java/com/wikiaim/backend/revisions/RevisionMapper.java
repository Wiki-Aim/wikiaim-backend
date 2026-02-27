package com.wikiaim.backend.revisions;

import jakarta.inject.Singleton;

@Singleton
public class RevisionMapper {

    public RevisionResponseDTO toDTO(PageRevision revision) {
        if (revision == null) {
            return null;
        }
        return new RevisionResponseDTO(
            revision.getId(),
            revision.getPage().getId(),
            revision.getProposedTitle(),
            revision.getCommitMessage(),
            revision.getStatus(),
            revision.getCreatedAt()
        );
    }
}