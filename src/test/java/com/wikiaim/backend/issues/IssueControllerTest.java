package com.wikiaim.backend.issues;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
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
}
