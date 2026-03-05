package com.wikiaim.backend.issues;

import com.wikiaim.backend.core.validation.EnumValid;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;

@Serdeable
@Introspected
public record UpdateIssueStatusDTO(
    @NotNull(message = "Le statut est obligatoire.")
    @EnumValid(enumClass = IssueStatus.class, message = "Statut invalide. Valeurs acceptées : OPEN, IN_PROGRESS, RESOLVED, CLOSED")
    String status
) {}
