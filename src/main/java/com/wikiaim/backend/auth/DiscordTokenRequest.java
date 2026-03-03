package com.wikiaim.backend.auth;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Serdeable
@Introspected
public record DiscordTokenRequest(
    @NotBlank String accessToken
) {}
