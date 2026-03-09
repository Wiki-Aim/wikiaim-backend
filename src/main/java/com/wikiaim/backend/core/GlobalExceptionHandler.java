package com.wikiaim.backend.core;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.authentication.AuthorizationException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class GlobalExceptionHandler {

    @Error(global = true, exception = IllegalArgumentException.class)
    public HttpResponse<ApiErrorDTO> handleIllegalArgument(HttpRequest<?> request, IllegalArgumentException e) {
        log.warn("Bad request on {} {}: {}", request.getMethod(), request.getPath(), e.getMessage());
        return HttpResponse.badRequest(
            new ApiErrorDTO(400, "Bad Request", "Requête invalide")
        );
    }

    @Error(global = true, exception = IllegalStateException.class)
    public HttpResponse<ApiErrorDTO> handleIllegalState(HttpRequest<?> request, IllegalStateException e) {
        log.warn("Illegal state on {} {}: {}", request.getMethod(), request.getPath(), e.getMessage());
        return HttpResponse.badRequest(
            new ApiErrorDTO(400, "Bad Request", "Opération non autorisée")
        );
    }

    @Error(global = true, exception = ConstraintViolationException.class)
    public HttpResponse<ApiErrorDTO> handleValidation(HttpRequest<?> request, ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .sorted()
            .reduce((a, b) -> a + "; " + b)
            .orElse("Validation error");
        return HttpResponse.badRequest(
            new ApiErrorDTO(400, "Bad Request", message)
        );
    }

    @Error(global = true, exception = AuthorizationException.class)
    public HttpResponse<ApiErrorDTO> handleAuthorization(HttpRequest<?> request, AuthorizationException e) {
        if (e.isForbidden()) {
            return HttpResponse.status(HttpStatus.FORBIDDEN).body(
                new ApiErrorDTO(403, "Forbidden", "Accès refusé")
            );
        }
        return HttpResponse.status(HttpStatus.UNAUTHORIZED).body(
            new ApiErrorDTO(401, "Unauthorized", "Authentification requise ou token expiré")
        );
    }

    @Error(global = true, exception = HttpStatusException.class)
    public HttpResponse<ApiErrorDTO> handleHttpStatus(HttpRequest<?> request, HttpStatusException e) {
        HttpStatus status = e.getStatus();
        return HttpResponse.status(status).body(
            new ApiErrorDTO(status.getCode(), status.getReason(), e.getMessage())
        );
    }

    @Error(global = true, exception = Exception.class)
    public HttpResponse<ApiErrorDTO> handleGeneric(HttpRequest<?> request, Exception e) {
        log.error("Unexpected error on {} {}", request.getMethod(), request.getPath(), e);
        return HttpResponse.serverError(
            new ApiErrorDTO(500, "Internal Server Error", "Une erreur interne est survenue")
        );
    }
}
