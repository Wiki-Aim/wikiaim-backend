package com.wikiaim.backend.pages;

import com.wikiaim.backend.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PageServiceTest {

    @Mock
    private PageRepository pageRepository;

    private final PageMapper pageMapper = new PageMapper();

    private PageService pageService;

    @BeforeEach
    void setUp() {
        pageService = new PageService(pageRepository, pageMapper);
    }

    @Test
    void getAllPublishedPages_shouldReturnMappedPages() {
        // Arrange
        User author = User.builder().id(UUID.randomUUID()).build();

        Page page = Page.builder()
            .id(UUID.randomUUID())
            .title("Guide du aim")
            .slug("guide-aim")
            .currentContent("{\"blocks\":[]}")
            .author(author)
            .isPublished(true)
            .build();

        when(pageRepository.findByIsPublishedTrue()).thenReturn(List.of(page));

        // Act
        List<PageResponseDTO> result = pageService.getAllPublishedPages();

        // Assert
        assertEquals(1, result.size());
        assertEquals(page.getId(), result.getFirst().id());
        assertEquals("Guide du aim", result.getFirst().title());
        assertEquals(author.getId(), result.getFirst().authorId());
    }

    @Test
    void getAllPublishedPages_shouldReturnEmptyListWhenNoPublishedPages() {
        // Arrange
        when(pageRepository.findByIsPublishedTrue()).thenReturn(List.of());

        // Act
        List<PageResponseDTO> result = pageService.getAllPublishedPages();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getPageBySlug_shouldReturnPageWhenFound() {
        // Arrange
        User author = User.builder().id(UUID.randomUUID()).build();

        Page page = Page.builder()
            .id(UUID.randomUUID())
            .title("Guide du aim")
            .slug("guide-aim")
            .currentContent("{\"blocks\":[]}")
            .author(author)
            .build();

        when(pageRepository.findBySlugAndIsPublishedTrue("guide-aim")).thenReturn(Optional.of(page));

        // Act
        Optional<PageResponseDTO> result = pageService.getPageBySlug("guide-aim");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(page.getId(), result.get().id());
        assertEquals("guide-aim", result.get().slug());
    }

    @Test
    void getPageBySlug_shouldReturnEmptyWhenNotFound() {
        // Arrange
        when(pageRepository.findBySlugAndIsPublishedTrue("slug-inconnu")).thenReturn(Optional.empty());

        // Act
        Optional<PageResponseDTO> result = pageService.getPageBySlug("slug-inconnu");

        // Assert
        assertTrue(result.isEmpty());
    }
}
