package com.wikiaim.backend.auth;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@Introspected
public record AuthResponseDTO(
    String backendToken,
    String userId,
    String role
) {}
