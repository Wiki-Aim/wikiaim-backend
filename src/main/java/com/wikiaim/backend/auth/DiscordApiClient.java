package com.wikiaim.backend.auth;

import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.client.annotation.Client;

@Client("discord")
public interface DiscordApiClient {

    @Get("/users/@me")
    DiscordUserProfile getUserProfile(@Header("Authorization") String authorization);
}
