package com.wikiaim.backend.core;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Error(global = true, exception = IllegalArgumentException.class)
    public HttpResponse<ApiErrorDTO> handleIllegalArgument(HttpRequest<?> request, IllegalArgumentException e) {
        return HttpResponse.badRequest(
            new ApiErrorDTO(400, "Bad Request", e.getMessage())
        );
    }

    @Error(global = true, exception = IllegalStateException.class)
    public HttpResponse<ApiErrorDTO> handleIllegalState(HttpRequest<?> request, IllegalStateException e) {
        return HttpResponse.badRequest(
            new ApiErrorDTO(400, "Bad Request", e.getMessage())
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

    @Error(global = true, exception = HttpStatusException.class)
    public HttpResponse<ApiErrorDTO> handleHttpStatus(HttpRequest<?> request, HttpStatusException e) {
        HttpStatus status = e.getStatus();
        return HttpResponse.status(status).body(
            new ApiErrorDTO(status.getCode(), status.getReason(), e.getMessage())
        );
    }

    @Error(global = true, exception = Exception.class)
    public HttpResponse<ApiErrorDTO> handleGeneric(HttpRequest<?> request, Exception e) {
        LOG.error("Unexpected error on {} {}", request.getMethod(), request.getPath(), e);
        return HttpResponse.serverError(
            new ApiErrorDTO(500, "Internal Server Error", "Une erreur interne est survenue")
        );
    }
}
