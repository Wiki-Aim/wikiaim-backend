package com.wikiaim.backend.core;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(environments = "test")
@Property(name = "micronaut.security.enabled", value = "false")
@Property(name = "spec.name", value = "GlobalExceptionHandlerTest")
class GlobalExceptionHandlerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void shouldReturn400ForIllegalArgumentException() {
        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.GET("/test/illegal-argument"),
                ApiErrorDTO.class
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        ApiErrorDTO body = exception.getResponse().getBody(ApiErrorDTO.class).orElse(null);
        assertNotNull(body);
        assertEquals(400, body.status());
        assertEquals("Bad Request", body.error());
        assertEquals("Ressource introuvable", body.message());
    }

    @Test
    void shouldReturn400ForIllegalStateException() {
        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.GET("/test/illegal-state"),
                ApiErrorDTO.class
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        ApiErrorDTO body = exception.getResponse().getBody(ApiErrorDTO.class).orElse(null);
        assertNotNull(body);
        assertEquals(400, body.status());
        assertEquals("Opération invalide", body.message());
    }

    @Test
    void shouldReturnCorrectStatusForHttpStatusException() {
        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.GET("/test/http-status"),
                ApiErrorDTO.class
            )
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        ApiErrorDTO body = exception.getResponse().getBody(ApiErrorDTO.class).orElse(null);
        assertNotNull(body);
        assertEquals(401, body.status());
        assertEquals("Token invalide", body.message());
    }

    @Test
    void shouldReturn400ForConstraintViolation() {
        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.POST("/test/validation", Map.of("name", "")),
                ApiErrorDTO.class
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        ApiErrorDTO body = exception.getResponse().getBody(ApiErrorDTO.class).orElse(null);
        assertNotNull(body);
        assertEquals(400, body.status());
        assertEquals("Bad Request", body.error());
    }

    @Test
    void shouldReturn500ForUnexpectedException() {
        HttpClientResponseException exception = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.GET("/test/unexpected"),
                ApiErrorDTO.class
            )
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
        ApiErrorDTO body = exception.getResponse().getBody(ApiErrorDTO.class).orElse(null);
        assertNotNull(body);
        assertEquals(500, body.status());
        assertEquals("Internal Server Error", body.error());
        assertEquals("Une erreur interne est survenue", body.message());
    }

    @Controller("/test")
    @Requires(property = "spec.name", value = "GlobalExceptionHandlerTest")
    static class TestController {

        @Get("/illegal-argument")
        public HttpResponse<Void> illegalArgument() {
            throw new IllegalArgumentException("Ressource introuvable");
        }

        @Get("/illegal-state")
        public HttpResponse<Void> illegalState() {
            throw new IllegalStateException("Opération invalide");
        }

        @Get("/http-status")
        public HttpResponse<Void> httpStatus() {
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Token invalide");
        }

        @Post("/validation")
        public HttpResponse<Void> validation(@Body @Valid TestValidationDTO dto) {
            return HttpResponse.ok();
        }

        @Get("/unexpected")
        public HttpResponse<Void> unexpected() {
            throw new RuntimeException("Erreur inattendue");
        }
    }

    @Serdeable
    record TestValidationDTO(@NotBlank String name) {}
}
