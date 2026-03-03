package com.wikiaim.backend.auth;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record DiscordUserProfile(
    String id,
    String username,
    String email,
    String avatar
) {}
