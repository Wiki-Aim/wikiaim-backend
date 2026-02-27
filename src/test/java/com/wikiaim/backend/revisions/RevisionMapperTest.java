package com.wikiaim.backend.revisions;

import com.wikiaim.backend.pages.Page;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RevisionMapperTest {

    private final RevisionMapper mapper = new RevisionMapper();

    @Test
    void shouldMapRevisionToDTO() {
        // Arrange
        Page page = Page.builder()
            .id(UUID.randomUUID())
            .build();

        PageRevision revision = PageRevision.builder()
            .id(UUID.randomUUID())
            .page(page)
            .proposedTitle("Nouveau titre")
            .commitMessage("fix: correction du titre")
            .status(RevisionStatus.PENDING)
            .createdAt(Instant.now())
            .build();

        // Act
        RevisionResponseDTO dto = mapper.toDTO(revision);

        // Assert
        assertNotNull(dto);
        assertEquals(revision.getId(), dto.id());
        assertEquals(page.getId(), dto.pageId());
        assertEquals("Nouveau titre", dto.proposedTitle());
        assertEquals("fix: correction du titre", dto.commitMessage());
        assertEquals(RevisionStatus.PENDING, dto.status());
        assertEquals(revision.getCreatedAt(), dto.createdAt());
    }

    @Test
    void shouldReturnNullForNullRevision() {
        assertNull(mapper.toDTO(null));
    }
}
