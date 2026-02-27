package com.wikiaim.backend.issues;

import com.wikiaim.backend.users.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IssueMapperTest {

    private final IssueMapper mapper = new IssueMapper();

    @Test
    void shouldMapIssueToDTO() {
        // Arrange
        User author = User.builder()
            .id(UUID.randomUUID())
            .build();

        Issue issue = Issue.builder()
            .id(UUID.randomUUID())
            .title("Bug d'affichage")
            .description("Le menu ne s'affiche pas correctement")
            .status(IssueStatus.OPEN)
            .author(author)
            .createdAt(Instant.now())
            .build();

        // Act
        IssueResponseDTO dto = mapper.toDTO(issue);

        // Assert
        assertNotNull(dto);
        assertEquals(issue.getId(), dto.id());
        assertEquals("Bug d'affichage", dto.title());
        assertEquals("Le menu ne s'affiche pas correctement", dto.description());
        assertEquals(IssueStatus.OPEN, dto.status());
        assertEquals(author.getId(), dto.authorId());
        assertEquals(issue.getCreatedAt(), dto.createdAt());
    }

    @Test
    void shouldHandleNullAuthor() {
        // Arrange
        Issue issue = Issue.builder()
            .id(UUID.randomUUID())
            .title("Titre")
            .description("Description")
            .status(IssueStatus.OPEN)
            .build();

        // Act
        IssueResponseDTO dto = mapper.toDTO(issue);

        // Assert
        assertNotNull(dto);
        assertNull(dto.authorId());
    }

    @Test
    void shouldReturnNullForNullIssue() {
        assertNull(mapper.toDTO(null));
    }
}
