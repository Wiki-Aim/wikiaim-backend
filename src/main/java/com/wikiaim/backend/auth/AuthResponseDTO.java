package com.wikiaim.backend.auth;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record AuthResponseDTO(
    String accessToken,
    String tokenType,
    String role
) {}
