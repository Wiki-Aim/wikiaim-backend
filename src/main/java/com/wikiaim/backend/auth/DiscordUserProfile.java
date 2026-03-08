package com.wikiaim.backend.auth;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@Introspected
public record DiscordUserProfile(
    String id,
    String username,
    String email,
    String avatar
) {}
