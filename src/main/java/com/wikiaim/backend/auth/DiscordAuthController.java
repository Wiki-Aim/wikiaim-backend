package com.wikiaim.backend.auth;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller("/api/v1/auth")
@Secured(SecurityRule.IS_ANONYMOUS)
@ExecuteOn(TaskExecutors.BLOCKING)
@Validated
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentification via Discord OAuth")
public class DiscordAuthController {

    private final DiscordAuthService discordAuthService;

    @Post("/discord")
    @Operation(summary = "Connexion via Discord", description = "Vérifie le token Discord auprès de l'API Discord et retourne un token JWT")
    @ApiResponse(responseCode = "200", description = "Authentification réussie, token JWT retourné")
    @ApiResponse(responseCode = "400", description = "Données invalides")
    @ApiResponse(responseCode = "401", description = "Token Discord invalide ou expiré")
    public HttpResponse<AuthResponseDTO> loginWithDiscord(@Body @Valid DiscordTokenRequest request) {
        AuthResponseDTO response = discordAuthService.loginWithDiscord(request);
        return HttpResponse.ok(response);
    }
}
