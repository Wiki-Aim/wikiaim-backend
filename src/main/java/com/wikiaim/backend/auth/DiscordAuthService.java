package com.wikiaim.backend.auth;

import com.wikiaim.backend.users.Role;
import com.wikiaim.backend.users.User;
import com.wikiaim.backend.users.UserRepository;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.token.jwt.generator.JwtTokenGenerator;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;

@Singleton
public class DiscordAuthService {

    private final UserRepository userRepository;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final DiscordApiClient discordApiClient;

    public DiscordAuthService(UserRepository userRepository,
                              JwtTokenGenerator jwtTokenGenerator,
                              DiscordApiClient discordApiClient) {
        this.userRepository = userRepository;
        this.jwtTokenGenerator = jwtTokenGenerator;
        this.discordApiClient = discordApiClient;
    }

    public AuthResponseDTO loginWithDiscord(DiscordTokenRequest request) {
        DiscordUserProfile profile = fetchDiscordProfile(request.accessToken());

        User user = userRepository.findByDiscordId(profile.id())
                                  .orElseGet(() -> {
                                      User newUser = User.builder()
                                              .discordId(profile.id())
                                              .username(profile.username())
                                              .email(profile.email())
                                              .avatarUrl(buildAvatarUrl(profile))
                                              .role(Role.USER)
                                              .build();
                                      return userRepository.save(newUser);
                                  });

        Authentication authentication = Authentication.build(
            user.getId().toString(),
            List.of(user.getRole().name()),
            Map.of("username", user.getUsername(), "avatar", user.getAvatarUrl() != null ? user.getAvatarUrl() : "")
        );

        String token = jwtTokenGenerator.generateToken(authentication, 3600)
            .orElseThrow(() -> new RuntimeException("Impossible de générer le token JWT"));

        return new AuthResponseDTO(token, "Bearer", user.getRole().name());
    }

    private DiscordUserProfile fetchDiscordProfile(String accessToken) {
        try {
            return discordApiClient.getUserProfile("Bearer " + accessToken);
        } catch (HttpClientResponseException e) {
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Token Discord invalide ou expiré");
        }
    }

    private String buildAvatarUrl(DiscordUserProfile profile) {
        if (profile.avatar() == null) {
            return null;
        }
        return "https://cdn.discordapp.com/avatars/" + profile.id() + "/" + profile.avatar() + ".png";
    }
}
