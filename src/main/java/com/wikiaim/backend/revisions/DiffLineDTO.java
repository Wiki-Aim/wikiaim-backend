package com.wikiaim.backend.revisions;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@Introspected
public record DiffLineDTO(
    DiffType type,
    String content
) {}
