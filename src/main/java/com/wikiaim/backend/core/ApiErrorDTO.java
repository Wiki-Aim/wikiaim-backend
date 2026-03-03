package com.wikiaim.backend.core;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record ApiErrorDTO(
    int status,
    String error,
    String message
) {}
