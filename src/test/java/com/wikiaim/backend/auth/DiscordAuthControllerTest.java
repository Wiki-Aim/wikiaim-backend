package com.wikiaim.backend.auth;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MicronautTest(environments = "test")
@Property(name = "micronaut.security.enabled", value = "false")
class DiscordAuthControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @MockBean(DiscordAuthService.class)
    DiscordAuthService discordAuthService() {
        return mock(DiscordAuthService.class);
    }

    @Inject
    DiscordAuthService discordAuthService;

    @BeforeEach
    void setUp() {
        reset(discordAuthService);
    }

    @Test
    void loginWithDiscord_shouldReturn200() {
        // Arrange
        AuthResponseDTO responseDTO = new AuthResponseDTO("jwt_token", "user-id-123", "USER");
        when(discordAuthService.loginWithDiscord(any(DiscordTokenRequest.class))).thenReturn(responseDTO);

        // Act
        HttpResponse<AuthResponseDTO> response = client.toBlocking().exchange(
            HttpRequest.POST("/api/v1/auth/discord", Map.of("accessToken", "valid_discord_token")),
            AuthResponseDTO.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());
        assertEquals("jwt_token", response.body().backendToken());
        assertEquals("user-id-123", response.body().userId());
        assertEquals("USER", response.body().role());
        verify(discordAuthService).loginWithDiscord(any(DiscordTokenRequest.class));
    }

    @Test
    void loginWithDiscord_shouldReturn401WhenDiscordTokenInvalid() {
        // Arrange
        when(discordAuthService.loginWithDiscord(any(DiscordTokenRequest.class)))
            .thenThrow(new HttpStatusException(HttpStatus.UNAUTHORIZED, "Token Discord invalide ou expiré"));

        // Act
        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.POST("/api/v1/auth/discord", Map.of("accessToken", "invalid_token")),
                AuthResponseDTO.class
            )
        );

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void loginWithDiscord_shouldReturn400WhenBodyIsEmpty() {
        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.POST("/api/v1/auth/discord", Map.of()),
                AuthResponseDTO.class
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(discordAuthService, never()).loginWithDiscord(any());
    }

    @Test
    void loginWithDiscord_shouldReturn400WhenTokenIsBlank() {
        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.POST("/api/v1/auth/discord", Map.of("accessToken", "")),
                AuthResponseDTO.class
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(discordAuthService, never()).loginWithDiscord(any());
    }
}
