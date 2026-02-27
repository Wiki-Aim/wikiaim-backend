package com.wikiaim.backend.issues;
import io.micronaut.serde.annotation.Serdeable;

import java.time.Instant;
import java.util.UUID;

@Serdeable
public record IssueResponseDTO(
    UUID id,
    String title,
    String description,
    IssueStatus status,
    UUID authorId,
    Instant createdAt
) {}
