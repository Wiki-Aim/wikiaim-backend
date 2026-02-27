package com.wikiaim.backend.revisions;

import io.micronaut.serde.annotation.Serdeable;

import java.time.Instant;
import java.util.UUID;

@Serdeable
public record RevisionResponseDTO(
    UUID id,
    UUID pageId,
    String proposedTitle,
    String commitMessage,
    RevisionStatus status,
    Instant createdAt
) {
}