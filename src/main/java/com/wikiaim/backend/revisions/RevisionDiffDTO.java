package com.wikiaim.backend.revisions;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Serdeable
@Introspected
public record RevisionDiffDTO(
    UUID revisionId,
    UUID pageId,
    String currentTitle,
    String proposedTitle,
    boolean titleChanged,
    List<DiffLineDTO> contentDiff,
    String commitMessage,
    RevisionStatus status,
    Instant createdAt
) {}
