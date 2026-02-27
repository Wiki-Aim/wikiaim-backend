package com.wikiaim.backend.pages;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest(environments = "test")
class PageControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void shouldReturnEmptyListWhenNoPages() {
        HttpRequest<?> request = HttpRequest.GET("/api/v1/pages");
        HttpResponse<List<PageResponseDTO>> response = client.toBlocking().exchange(
            request,
            Argument.listOf(PageResponseDTO.class)
        );

        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.body());
    }

    @Test
    void shouldReturn404ForUnknownSlug() {
        HttpRequest<?> request = HttpRequest.GET("/api/v1/pages/slug-inconnu");

        io.micronaut.http.client.exceptions.HttpClientResponseException exception =
            org.junit.jupiter.api.Assertions.assertThrows(
                io.micronaut.http.client.exceptions.HttpClientResponseException.class,
                () -> client.toBlocking().exchange(request, PageResponseDTO.class)
            );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}
