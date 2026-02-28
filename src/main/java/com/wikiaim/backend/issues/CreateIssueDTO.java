package com.wikiaim.backend.issues;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Serdeable
@Introspected
public record CreateIssueDTO(
    @NotBlank(message = "Le titre ne peut pas être vide.")
    @Size(min = 3, max = 255, message = "Le titre doit faire entre 3 et 255 caractères.")
    String title,

    @NotBlank(message = "La description ne peut pas être vide.")
    @Size(min = 10, max = 5000, message = "La description doit faire entre 10 et 5000 caractères.")
    String description,

    @NotNull(message = "L'ID de l'auteur est obligatoire.")
    UUID authorId
) {
}
