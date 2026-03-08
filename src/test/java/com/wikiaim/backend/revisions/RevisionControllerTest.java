package com.wikiaim.backend.revisions;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.token.jwt.generator.JwtTokenGenerator;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MicronautTest(environments = "test")
class RevisionControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    JwtTokenGenerator jwtTokenGenerator;

    @MockBean(RevisionService.class)
    RevisionService revisionService() {
        return mock(RevisionService.class);
    }

    @Inject
    RevisionService revisionService;

    private String token;
    private UUID authenticatedUserId;

    @BeforeEach
    void setUp() {
        reset(revisionService);
        authenticatedUserId = UUID.randomUUID();
        token = generateToken(authenticatedUserId);
    }

    private String generateToken(UUID userId) {
        Authentication auth = Authentication.build(userId.toString(), List.of("USER"));
        return jwtTokenGenerator.generateToken(auth, 3600).orElseThrow();
    }

    @Test
    void proposeRevision_shouldReturn201() {
        // Arrange
        UUID revisionId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();

        RevisionResponseDTO responseDTO = new RevisionResponseDTO(
            revisionId, pageId, "Titre proposé", "feat: nouveau contenu", RevisionStatus.PENDING, Instant.now()
        );

        when(revisionService.proposeRevision(any(ProposeRevisionDTO.class))).thenReturn(responseDTO);

        Map<String, Object> body = Map.of(
            "pageId", pageId,
            "authorId", UUID.randomUUID(),
            "proposedTitle", "Titre proposé",
            "proposedContent", "{\"blocks\":[]}",
            "commitMessage", "feat: nouveau contenu"
        );

        // Act
        HttpResponse<RevisionResponseDTO> response = client.toBlocking().exchange(
            HttpRequest.POST("/api/v1/revisions", body).bearerAuth(token),
            RevisionResponseDTO.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertNotNull(response.body());
        assertEquals(revisionId, response.body().id());
        assertEquals(RevisionStatus.PENDING, response.body().status());
        verify(revisionService).proposeRevision(any(ProposeRevisionDTO.class));
    }

    @Test
    void listPending_shouldReturn200WithList() {
        // Arrange
        RevisionResponseDTO dto = new RevisionResponseDTO(
            UUID.randomUUID(), UUID.randomUUID(), "Titre", "msg", RevisionStatus.PENDING, Instant.now()
        );

        when(revisionService.getPendingRevisions()).thenReturn(List.of(dto));

        // Act
        HttpResponse<List<RevisionResponseDTO>> response = client.toBlocking().exchange(
            HttpRequest.GET("/api/v1/revisions/pending").bearerAuth(token),
            Argument.listOf(RevisionResponseDTO.class)
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());
        assertEquals(1, response.body().size());
        assertEquals(dto.id(), response.body().getFirst().id());
    }

    @Test
    void listPending_shouldReturn200WithEmptyList() {
        // Arrange
        when(revisionService.getPendingRevisions()).thenReturn(List.of());

        // Act
        HttpResponse<List<RevisionResponseDTO>> response = client.toBlocking().exchange(
            HttpRequest.GET("/api/v1/revisions/pending").bearerAuth(token),
            Argument.listOf(RevisionResponseDTO.class)
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());
        assertTrue(response.body().isEmpty());
    }

    @Test
    void approveRevision_shouldReturn200() {
        // Arrange
        UUID revisionId = UUID.randomUUID();

        doNothing().when(revisionService).approveRevision(revisionId, authenticatedUserId);

        String url = "/api/v1/revisions/" + revisionId + "/approve";

        // Act
        HttpResponse<?> response = client.toBlocking().exchange(
            HttpRequest.POST(url, "").bearerAuth(token)
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatus());
        verify(revisionService).approveRevision(revisionId, authenticatedUserId);
    }

    @Test
    void approveRevision_shouldReturn400WhenRevisionNotFound() {
        // Arrange
        UUID revisionId = UUID.randomUUID();

        doThrow(new IllegalArgumentException("Révision introuvable"))
            .when(revisionService).approveRevision(eq(revisionId), any(UUID.class));

        String url = "/api/v1/revisions/" + revisionId + "/approve";

        // Act
        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(HttpRequest.POST(url, "").bearerAuth(token))
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void proposeRevision_shouldReturn400WhenBodyIsEmpty() {
        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.POST("/api/v1/revisions", Map.of()).bearerAuth(token),
                RevisionResponseDTO.class
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(revisionService, never()).proposeRevision(any());
    }

    @Test
    void proposeRevision_shouldReturn400WhenTitleIsBlank() {
        Map<String, Object> body = Map.of(
            "pageId", UUID.randomUUID(),
            "authorId", UUID.randomUUID(),
            "proposedTitle", "",
            "proposedContent", "{\"blocks\":[]}"
        );

        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.POST("/api/v1/revisions", body).bearerAuth(token),
                RevisionResponseDTO.class
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(revisionService, never()).proposeRevision(any());
    }

    @Test
    void proposeRevision_shouldReturn400WhenTitleTooShort() {
        Map<String, Object> body = Map.of(
            "pageId", UUID.randomUUID(),
            "authorId", UUID.randomUUID(),
            "proposedTitle", "AB",
            "proposedContent", "{\"blocks\":[]}"
        );

        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.POST("/api/v1/revisions", body).bearerAuth(token),
                RevisionResponseDTO.class
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(revisionService, never()).proposeRevision(any());
    }

    @Test
    void proposeRevision_shouldReturn400WhenContentIsBlank() {
        Map<String, Object> body = Map.of(
            "pageId", UUID.randomUUID(),
            "authorId", UUID.randomUUID(),
            "proposedTitle", "Titre valide",
            "proposedContent", ""
        );

        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.POST("/api/v1/revisions", body).bearerAuth(token),
                RevisionResponseDTO.class
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(revisionService, never()).proposeRevision(any());
    }

    @Test
    void approveRevision_shouldReturn400WhenRevisionNotPending() {
        // Arrange
        UUID revisionId = UUID.randomUUID();

        doThrow(new IllegalStateException("Seule une révision PENDING peut être approuvée."))
            .when(revisionService).approveRevision(eq(revisionId), any(UUID.class));

        String url = "/api/v1/revisions/" + revisionId + "/approve";

        // Act
        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(HttpRequest.POST(url, "").bearerAuth(token))
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void getDiff_shouldReturn200() {
        // Arrange
        UUID revisionId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();

        RevisionDiffDTO diffDTO = new RevisionDiffDTO(
            revisionId, pageId, "Titre actuel", "Titre modifié", true,
            List.of(
                new DiffLineDTO(DiffType.EQUAL, "Ligne inchangée"),
                new DiffLineDTO(DiffType.DELETE, "Ligne supprimée"),
                new DiffLineDTO(DiffType.INSERT, "Ligne ajoutée")
            ),
            "fix: correction", RevisionStatus.PENDING, Instant.now()
        );

        when(revisionService.getRevisionDiff(revisionId)).thenReturn(diffDTO);

        // Act
        HttpResponse<RevisionDiffDTO> response = client.toBlocking().exchange(
            HttpRequest.GET("/api/v1/revisions/" + revisionId + "/diff").bearerAuth(token),
            RevisionDiffDTO.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());
        assertEquals(revisionId, response.body().revisionId());
        assertTrue(response.body().titleChanged());
        assertEquals(3, response.body().contentDiff().size());
        verify(revisionService).getRevisionDiff(revisionId);
    }

    @Test
    void getDiff_shouldReturn400WhenRevisionNotFound() {
        // Arrange
        UUID revisionId = UUID.randomUUID();

        when(revisionService.getRevisionDiff(revisionId))
            .thenThrow(new IllegalArgumentException("Révision introuvable"));

        // Act
        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.GET("/api/v1/revisions/" + revisionId + "/diff").bearerAuth(token),
                RevisionDiffDTO.class
            )
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void shouldReturn401WhenNoToken() {
        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.GET("/api/v1/revisions/pending")
            )
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }
}
