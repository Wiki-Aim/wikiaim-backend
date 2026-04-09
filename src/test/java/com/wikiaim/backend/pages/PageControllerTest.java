package com.wikiaim.backend.pages;

import io.micronaut.context.annotation.Property;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MicronautTest(environments = "test")
@Property(name = "micronaut.security.enabled", value = "false")
class PageControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @MockBean(PageService.class)
    PageService pageService() {
        return mock(PageService.class);
    }

    @Inject
    PageService pageService;

    @BeforeEach
    void setUp() {
        reset(pageService);
    }

    @Test
    void getPage_shouldReturn200WhenFound() {
        // Arrange
        PageResponseDTO dto = new PageResponseDTO(
            UUID.randomUUID(), "Guide du aim", "guide-aim",
            "{\"blocks\":[]}", UUID.randomUUID(), "aim", Instant.now()
        );

        when(pageService.getPageBySlug("guide-aim")).thenReturn(Optional.of(dto));

        // Act
        HttpResponse<PageResponseDTO> response = client.toBlocking().exchange(
            HttpRequest.GET("/api/v1/pages/guide-aim"),
            PageResponseDTO.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());
        assertEquals("guide-aim", response.body().slug());
        assertEquals("aim", response.body().categorySlug());
    }

    @Test
    void getPage_shouldReturn404WhenNotFound() {
        // Arrange
        when(pageService.getPageBySlug("slug-inconnu")).thenReturn(Optional.empty());

        // Act
        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.GET("/api/v1/pages/slug-inconnu"),
                PageResponseDTO.class
            )
        );

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}
