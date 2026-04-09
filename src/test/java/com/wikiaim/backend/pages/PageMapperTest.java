package com.wikiaim.backend.pages;

import com.wikiaim.backend.categories.Category;
import com.wikiaim.backend.users.User;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(environments = "test", startApplication = false)
@Property(name = "micronaut.security.enabled", value = "false")
class PageMapperTest {

    @Inject
    PageMapper mapper;

    @Test
    void shouldMapPageToDTO() {
        // Arrange
        User author = User.builder()
            .id(UUID.randomUUID())
            .build();

        Category category = Category.builder()
            .id(UUID.randomUUID())
            .slug("aim")
            .build();

        Page page = Page.builder()
            .id(UUID.randomUUID())
            .title("Titre de test")
            .slug("titre-de-test")
            .currentContent("{\"blocks\":[]}")
            .author(author)
            .category(category)
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
        assertEquals("aim", dto.categorySlug());
    }

    @Test
    void shouldHandleNullAuthorAndCategory() {
        // Arrange
        Page page = Page.builder()
            .id(UUID.randomUUID())
            .build();

        // Act
        PageResponseDTO dto = mapper.toDTO(page);

        // Assert
        assertNull(dto.authorId());
        assertNull(dto.categorySlug());
    }

    @Test
    void shouldMapPageToSummaryDTO() {
        // Arrange
        User author = User.builder()
            .id(UUID.randomUUID())
            .build();

        Category category = Category.builder()
            .id(UUID.randomUUID())
            .slug("aim")
            .build();

        Page page = Page.builder()
            .id(UUID.randomUUID())
            .title("Titre de test")
            .slug("titre-de-test")
            .currentContent("{\"blocks\":[]}")
            .author(author)
            .category(category)
            .build();

        // Act
        PageSummaryDTO dto = mapper.toSummaryDTO(page);

        // Assert
        assertNotNull(dto);
        assertEquals(page.getId(), dto.id());
        assertEquals("Titre de test", dto.title());
        assertEquals("titre-de-test", dto.slug());
        assertEquals(author.getId(), dto.authorId());
        assertEquals("aim", dto.categorySlug());
    }

    @Test
    void shouldHandleNullAuthorAndCategoryInSummary() {
        // Arrange
        Page page = Page.builder()
            .id(UUID.randomUUID())
            .build();

        // Act
        PageSummaryDTO dto = mapper.toSummaryDTO(page);

        // Assert
        assertNull(dto.authorId());
        assertNull(dto.categorySlug());
    }
}
