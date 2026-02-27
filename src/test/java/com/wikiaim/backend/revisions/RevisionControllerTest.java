package com.wikiaim.backend.revisions;

import io.micronaut.context.annotation.Property;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
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
@Property(name = "micronaut.security.enabled", value = "false") // TODO : RevisionController n'a pas encore de @Secured
class RevisionControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @MockBean(RevisionService.class)
    RevisionService revisionService() {
        return mock(RevisionService.class);
    }

    @Inject
    RevisionService revisionService;

    @BeforeEach
    void setUp() {
        reset(revisionService);
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
            HttpRequest.POST("/api/v1/revisions", body),
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
            HttpRequest.GET("/api/v1/revisions/pending"),
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
            HttpRequest.GET("/api/v1/revisions/pending"),
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
        UUID reviewerId = UUID.randomUUID();

        doNothing().when(revisionService).approveRevision(revisionId, reviewerId);

        String url = "/api/v1/revisions/" + revisionId + "/approve?reviewerId=" + reviewerId;

        // Act
        HttpResponse<?> response = client.toBlocking().exchange(
            HttpRequest.POST(url, "")
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatus());
        verify(revisionService).approveRevision(revisionId, reviewerId);
    }

    @Test
    void approveRevision_shouldReturn400WhenRevisionNotFound() {
        // Arrange
        UUID revisionId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();

        doThrow(new IllegalArgumentException("Révision introuvable"))
            .when(revisionService).approveRevision(revisionId, reviewerId);

        String url = "/api/v1/revisions/" + revisionId + "/approve?reviewerId=" + reviewerId;

        // Act
        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(HttpRequest.POST(url, ""))
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void approveRevision_shouldReturn400WhenRevisionNotPending() {
        // Arrange
        UUID revisionId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();

        doThrow(new IllegalStateException("Seule une révision PENDING peut être approuvée."))
            .when(revisionService).approveRevision(revisionId, reviewerId);

        String url = "/api/v1/revisions/" + revisionId + "/approve?reviewerId=" + reviewerId;

        // Act
        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(HttpRequest.POST(url, ""))
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }
}
