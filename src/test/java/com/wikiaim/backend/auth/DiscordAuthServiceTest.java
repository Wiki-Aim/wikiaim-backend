package com.wikiaim.backend.auth;

import com.wikiaim.backend.users.Role;
import com.wikiaim.backend.users.User;
import com.wikiaim.backend.users.UserRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.token.jwt.generator.JwtTokenGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscordAuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    JwtTokenGenerator jwtTokenGenerator;

    @Mock
    DiscordApiClient discordApiClient;

    @InjectMocks
    DiscordAuthService discordAuthService;

    private DiscordUserProfile discordProfile;
    private User existingUser;

    @BeforeEach
    void setUp() {
        discordProfile = new DiscordUserProfile("123456789", "Erwan", "erwan@test.com", "abc123hash");
        existingUser = User.builder()
            .id(UUID.randomUUID())
            .discordId("123456789")
            .username("Erwan")
            .email("erwan@test.com")
            .avatarUrl("https://cdn.discordapp.com/avatars/123456789/abc123hash.png")
            .role(Role.USER)
            .build();
    }

    @Test
    void loginWithDiscord_shouldCreateNewUserWhenNotFound() {
        // Arrange
        DiscordTokenRequest request = new DiscordTokenRequest("valid_token");

        when(discordApiClient.getUserProfile("Bearer valid_token")).thenReturn(discordProfile);
        when(userRepository.findByDiscordId("123456789")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(jwtTokenGenerator.generateToken(any(), eq(3600))).thenReturn(Optional.of("jwt_token"));

        // Act
        AuthResponseDTO result = discordAuthService.loginWithDiscord(request);

        // Assert
        assertNotNull(result);
        assertEquals("jwt_token", result.backendToken());
        assertEquals(existingUser.getId().toString(), result.userId());
        assertEquals("USER", result.role());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("123456789", savedUser.getDiscordId());
        assertEquals("Erwan", savedUser.getUsername());
        assertEquals("erwan@test.com", savedUser.getEmail());
        assertEquals("https://cdn.discordapp.com/avatars/123456789/abc123hash.png", savedUser.getAvatarUrl());
        assertEquals(Role.USER, savedUser.getRole());
    }

    @Test
    void loginWithDiscord_shouldReturnExistingUser() {
        // Arrange
        DiscordTokenRequest request = new DiscordTokenRequest("valid_token");

        when(discordApiClient.getUserProfile("Bearer valid_token")).thenReturn(discordProfile);
        when(userRepository.findByDiscordId("123456789")).thenReturn(Optional.of(existingUser));
        when(jwtTokenGenerator.generateToken(any(), eq(3600))).thenReturn(Optional.of("jwt_token"));

        // Act
        AuthResponseDTO result = discordAuthService.loginWithDiscord(request);

        // Assert
        assertNotNull(result);
        assertEquals("jwt_token", result.backendToken());
        assertEquals(existingUser.getId().toString(), result.userId());
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginWithDiscord_shouldThrowUnauthorizedWhenTokenInvalid() {
        // Arrange
        DiscordTokenRequest request = new DiscordTokenRequest("invalid_token");

        when(discordApiClient.getUserProfile("Bearer invalid_token"))
            .thenThrow(new HttpClientResponseException("Unauthorized", HttpResponse.status(HttpStatus.UNAUTHORIZED)));

        // Act & Assert
        HttpStatusException exception = assertThrows(
            HttpStatusException.class,
            () -> discordAuthService.loginWithDiscord(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        verify(userRepository, never()).findByDiscordId(any());
    }

    @Test
    void loginWithDiscord_shouldHandleNullAvatar() {
        // Arrange
        DiscordTokenRequest request = new DiscordTokenRequest("valid_token");
        DiscordUserProfile profileWithoutAvatar = new DiscordUserProfile("123456789", "Erwan", "erwan@test.com", null);
        User userWithoutAvatar = User.builder()
            .id(UUID.randomUUID())
            .discordId("123456789")
            .username("Erwan")
            .email("erwan@test.com")
            .avatarUrl(null)
            .role(Role.USER)
            .build();

        when(discordApiClient.getUserProfile("Bearer valid_token")).thenReturn(profileWithoutAvatar);
        when(userRepository.findByDiscordId("123456789")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(userWithoutAvatar);
        when(jwtTokenGenerator.generateToken(any(), eq(3600))).thenReturn(Optional.of("jwt_token"));

        // Act
        AuthResponseDTO result = discordAuthService.loginWithDiscord(request);

        // Assert
        assertNotNull(result);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertNull(userCaptor.getValue().getAvatarUrl());
    }

    @Test
    void loginWithDiscord_shouldThrowWhenJwtGenerationFails() {
        // Arrange
        DiscordTokenRequest request = new DiscordTokenRequest("valid_token");

        when(discordApiClient.getUserProfile("Bearer valid_token")).thenReturn(discordProfile);
        when(userRepository.findByDiscordId("123456789")).thenReturn(Optional.of(existingUser));
        when(jwtTokenGenerator.generateToken(any(), eq(3600))).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> discordAuthService.loginWithDiscord(request)
        );

        assertEquals("Impossible de générer le token JWT", exception.getMessage());
    }
}
