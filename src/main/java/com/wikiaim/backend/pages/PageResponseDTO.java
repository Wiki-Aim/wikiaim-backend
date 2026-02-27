package com.wikiaim.backend.pages;

import io.micronaut.serde.annotation.Serdeable;

import java.time.Instant;
import java.util.UUID;

@Serdeable
public record PageResponseDTO(
    UUID id,
    String title,
    String slug,
    String currentContent,
    UUID authorId,
    Instant updatedAt
) {
}
