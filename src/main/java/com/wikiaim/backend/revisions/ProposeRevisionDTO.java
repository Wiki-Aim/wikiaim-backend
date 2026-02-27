package com.wikiaim.backend.revisions;

import io.micronaut.serde.annotation.Serdeable;

import java.util.UUID;

@Serdeable
public record ProposeRevisionDTO(
    UUID pageId,
    UUID authorId,
    String proposedTitle,
    String proposedContent,
    String commitMessage
) {
}