package com.wikiaim.backend.revisions;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Serdeable
@Introspected
public record ProposeRevisionDTO(
    @NotNull(message = "L'ID de la page est obligatoire.")
    UUID pageId,

    @NotNull(message = "L'ID de l'auteur est obligatoire.")
    UUID authorId,

    @NotBlank(message = "Le titre proposé ne peut pas être vide.")
    @Size(min = 3, max = 150, message = "Le titre doit faire entre 3 et 150 caractères.")
    String proposedTitle,

    @NotBlank(message = "Le contenu (JSON) ne peut pas être vide.")
    @Size(max = 5_000_000, message = "Le contenu ne doit pas dépasser 5 Mo.")
    String proposedContent,

    @Size(max = 500, message = "Le message de commit ne doit pas dépasser 500 caractères.")
    String commitMessage
) {
}