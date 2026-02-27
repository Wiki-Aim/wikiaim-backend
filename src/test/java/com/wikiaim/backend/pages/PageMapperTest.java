package com.wikiaim.backend.pages;

import com.wikiaim.backend.users.User;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PageMapperTest {

    private final PageMapper mapper = new PageMapper();

    @Test
    void shouldMapPageToDTO() {
        // Arrange
        User author = User.builder()
            .id(UUID.randomUUID())
            .build();

        Page page = Page.builder()
            .id(UUID.randomUUID())
            .title("Titre de test")
            .slug("titre-de-test")
            .currentContent("{\"blocks\":[]}")
            .author(author)
            .build();

        // Act
        PageResponseDTO dto = mapper.toDTO(page);

        // Assert
        assertNotNull(dto);
        assertEquals(page.getId(), dto.id());
        assertEquals("Titre de test", dto.title());
        assertEquals("titre-de-test", dto.slug());
        assertEquals("{\"blocks\":[]}", dto.currentContent());
        assertEquals(author.getId(), dto.authorId());
    }

    @Test
    void shouldHandleNullAuthor() {
        // Arrange
        Page page = Page.builder()
            .id(UUID.randomUUID())
            .build();

        // Act
        PageResponseDTO dto = mapper.toDTO(page);

        // Assert
        assertNull(dto.authorId());
    }
}
