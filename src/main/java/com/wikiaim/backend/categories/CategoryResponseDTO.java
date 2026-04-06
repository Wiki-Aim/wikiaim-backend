package com.wikiaim.backend.categories;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

import java.util.UUID;

@Serdeable
@Introspected
public record CategoryResponseDTO(
    UUID id,
    String name,
    String slug,
    String description
) {
}
