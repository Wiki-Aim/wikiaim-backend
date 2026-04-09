package com.wikiaim.backend.pages;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

import java.time.Instant;
import java.util.UUID;

@Serdeable
@Introspected
public record PageSummaryDTO(
    UUID id,
    String title,
    String slug,
    UUID authorId,
    String categorySlug,
    Instant updatedAt
) {
}
