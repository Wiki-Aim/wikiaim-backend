package com.wikiaim.backend.auth;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.client.annotation.Client;

@Client("discord")
@Header(name = "User-Agent", value = "WikiAim Backend (https://wikiaim.fr, 0.1)")
@Header(name = "Accept", value = "application/json")
public interface DiscordApiClient {

    @Get("/api/v10/users/@me")
    DiscordUserProfile getUserProfile(@Header("Authorization") String authorization);
}
