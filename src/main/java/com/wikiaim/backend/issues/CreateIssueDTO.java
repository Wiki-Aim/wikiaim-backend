package com.wikiaim.backend.issues;
import io.micronaut.serde.annotation.Serdeable;

import java.util.UUID;

@Serdeable
public record CreateIssueDTO(
    String title,
    String description,
    UUID authorId
) {
}