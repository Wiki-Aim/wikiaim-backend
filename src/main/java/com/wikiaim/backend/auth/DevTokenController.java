package com.wikiaim.backend.auth;

import com.wikiaim.backend.users.Role;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.token.jwt.generator.JwtTokenGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller("/api/v1/dev")
@Secured(SecurityRule.IS_ANONYMOUS)
@Tag(name = "Dev", description = "Endpoints de développement — indisponibles en production")
@Requires(env = "local")
@RequiredArgsConstructor
public class DevTokenController {

    private final JwtTokenGenerator jwtTokenGenerator;

    @Post("/token")
    @Operation(
        summary = "Générer un token JWT de test",
        description = "Génère un token JWT pour le rôle spécifié. Disponible uniquement en environnement local."
    )
    @ApiResponse(responseCode = "200", description = "Token JWT généré")
    public AuthResponseDTO generateToken(@QueryValue(defaultValue = "USER") Role role) {
        UUID userId = UUID.randomUUID();

        Authentication authentication = Authentication.build(
            userId.toString(),
            List.of(role.name()),
            Map.of("username", "dev-user", "avatar", "")
        );

        String token = jwtTokenGenerator.generateToken(authentication, 3600)
            .orElseThrow(() -> new RuntimeException("Impossible de générer le token JWT"));

        return new AuthResponseDTO(token, userId.toString(), role.name());
    }
}
