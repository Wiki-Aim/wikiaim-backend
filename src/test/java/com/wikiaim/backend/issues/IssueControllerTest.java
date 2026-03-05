package com.wikiaim.backend.issues;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@MicronautTest(environments = "test")
@Property(name = "micronaut.security.enabled", value = "false")
class IssueControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @MockBean(IssueService.class)
    IssueService issueService() {
        return mock(IssueService.class);
    }

    @Inject
    IssueService issueService;

    @BeforeEach
    void setUp() {
        reset(issueService);
    }

    @Test
    void createIssue_shouldReturn201() {
        // Arrange
        UUID issueId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        IssueResponseDTO responseDTO = new IssueResponseDTO(
            issueId, "Bug d'affichage", "Le menu ne s'affiche pas",
            IssueStatus.OPEN, authorId, Instant.now()
        );

        when(issueService.createIssue(any(CreateIssueDTO.class))).thenReturn(responseDTO);

        Map<String, Object> body = Map.of(
            "title", "Bug d'affichage",
            "description", "Le menu ne s'affiche pas",
            "authorId", authorId
        );

        // Act
        HttpResponse<IssueResponseDTO> response = client.toBlocking().exchange(
            HttpRequest.POST("/api/v1/issues", body),
            IssueResponseDTO.class
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertNotNull(response.body());
        assertEquals(issueId, response.body().id());
        assertEquals(IssueStatus.OPEN, response.body().status());
        verify(issueService).createIssue(any(CreateIssueDTO.class));
    }

    @Test
    void createIssue_shouldReturn400WhenBodyIsEmpty() {
        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.POST("/api/v1/issues", Map.of()),
                IssueResponseDTO.class
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(issueService, never()).createIssue(any());
    }

    @Test
    void createIssue_shouldReturn400WhenTitleIsBlank() {
        Map<String, Object> body = Map.of(
            "title", "",
            "description", "Description assez longue pour être valide",
            "authorId", UUID.randomUUID()
        );

        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.POST("/api/v1/issues", body),
                IssueResponseDTO.class
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(issueService, never()).createIssue(any());
    }

    @Test
    void createIssue_shouldReturn400WhenDescriptionTooShort() {
        Map<String, Object> body = Map.of(
            "title", "Bug valide",
            "description", "Court",
            "authorId", UUID.randomUUID()
        );

        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.POST("/api/v1/issues", body),
                IssueResponseDTO.class
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(issueService, never()).createIssue(any());
    }

    @Test
    void listOpenIssues_shouldReturn200WithList() {
        // Arrange
        IssueResponseDTO dto = new IssueResponseDTO(
            UUID.randomUUID(), "Bug", "Description", IssueStatus.OPEN, UUID.randomUUID(), Instant.now()
        );

        when(issueService.getOpenIssues()).thenReturn(List.of(dto));

        // Act
        HttpResponse<List<IssueResponseDTO>> response = client.toBlocking().exchange(
            HttpRequest.GET("/api/v1/issues/open"),
            Argument.listOf(IssueResponseDTO.class)
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());
        assertEquals(1, response.body().size());
        assertEquals(dto.id(), response.body().getFirst().id());
    }

    @Test
    void listOpenIssues_shouldReturn200WithEmptyList() {
        // Arrange
        when(issueService.getOpenIssues()).thenReturn(List.of());

        // Act
        HttpResponse<List<IssueResponseDTO>> response = client.toBlocking().exchange(
            HttpRequest.GET("/api/v1/issues/open"),
            Argument.listOf(IssueResponseDTO.class)
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());
        assertTrue(response.body().isEmpty());
    }

    @Test
    void updateStatus_shouldReturn200() {
        // Arrange
        UUID issueId = UUID.randomUUID();
        IssueResponseDTO responseDTO = new IssueResponseDTO(
            issueId, "Bug", "Description", IssueStatus.IN_PROGRESS, UUID.randomUUID(), Instant.now()
        );

        when(issueService.updateStatus(eq(issueId), any(UpdateIssueStatusDTO.class))).thenReturn(responseDTO);

        Map<String, Object> body = Map.of("status", "IN_PROGRESS");

        // Act
        HttpResponse<IssueResponseDTO> response = client.toBlocking().exchange(
            HttpRequest.PATCH("/api/v1/issues/" + issueId + "/status", body),
            IssueResponseDTO.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());
        assertEquals(IssueStatus.IN_PROGRESS, response.body().status());
        verify(issueService).updateStatus(eq(issueId), any(UpdateIssueStatusDTO.class));
    }

    @Test
    void updateStatus_shouldReturn400WhenStatusInvalid() {
        Map<String, Object> body = Map.of("status", "TOTO");

        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.PATCH("/api/v1/issues/" + UUID.randomUUID() + "/status", body),
                IssueResponseDTO.class
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(issueService, never()).updateStatus(any(), any());
    }

    @Test
    void updateStatus_shouldReturn400WhenBodyEmpty() {
        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.PATCH("/api/v1/issues/" + UUID.randomUUID() + "/status", Map.of()),
                IssueResponseDTO.class
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(issueService, never()).updateStatus(any(), any());
    }
}
