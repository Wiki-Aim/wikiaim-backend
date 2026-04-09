package com.wikiaim.backend.pages;

import com.wikiaim.backend.categories.Category;
import com.wikiaim.backend.categories.CategoryRepository;
import com.wikiaim.backend.users.User;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MicronautTest(environments = "test", startApplication = false)
@Property(name = "micronaut.security.enabled", value = "false")
class PageServiceTest {

    @MockBean(PageRepository.class)
    PageRepository pageRepository() {
        return mock(PageRepository.class);
    }

    @MockBean(CategoryRepository.class)
    CategoryRepository categoryRepository() {
        return mock(CategoryRepository.class);
    }

    @Inject
    PageRepository pageRepository;

    @Inject
    CategoryRepository categoryRepository;

    @Inject
    PageService pageService;

    @BeforeEach
    void setUp() {
        reset(pageRepository, categoryRepository);
    }

    @Test
    void getPageBySlug_shouldReturnPageWhenFound() {
        // Arrange
        User author = User.builder().id(UUID.randomUUID()).build();
        Category category = Category.builder().id(UUID.randomUUID()).slug("aim").build();

        Page page = Page.builder()
            .id(UUID.randomUUID())
            .title("Guide du aim")
            .slug("guide-aim")
            .currentContent("{\"blocks\":[]}")
            .author(author)
            .category(category)
            .build();

        when(pageRepository.findBySlugAndIsPublishedTrue("guide-aim")).thenReturn(Optional.of(page));

        // Act
        Optional<PageResponseDTO> result = pageService.getPageBySlug("guide-aim");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(page.getId(), result.get().id());
        assertEquals("guide-aim", result.get().slug());
        assertEquals("aim", result.get().categorySlug());
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

    @Test
    void getPagesByCategorySlug_shouldReturnSummaries() {
        // Arrange
        Category category = Category.builder().id(UUID.randomUUID()).slug("aim").build();
        User author = User.builder().id(UUID.randomUUID()).build();

        Page page = Page.builder()
            .id(UUID.randomUUID())
            .title("Guide du aim")
            .slug("guide-aim")
            .currentContent("{\"blocks\":[]}")
            .author(author)
            .category(category)
            .isPublished(true)
            .build();

        when(categoryRepository.findBySlug("aim")).thenReturn(Optional.of(category));
        when(pageRepository.findByCategoryAndIsPublishedTrue(category)).thenReturn(List.of(page));

        // Act
        Optional<List<PageSummaryDTO>> result = pageService.getPagesByCategorySlug("aim");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals("Guide du aim", result.get().getFirst().title());
        assertEquals("aim", result.get().getFirst().categorySlug());
    }

    @Test
    void getPagesByCategorySlug_shouldReturnEmptyWhenCategoryNotFound() {
        // Arrange
        when(categoryRepository.findBySlug("inconnu")).thenReturn(Optional.empty());

        // Act
        Optional<List<PageSummaryDTO>> result = pageService.getPagesByCategorySlug("inconnu");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void searchPagesByCategorySlug_shouldReturnMatchingPages() {
        // Arrange
        Category category = Category.builder().id(UUID.randomUUID()).slug("aim").build();
        User author = User.builder().id(UUID.randomUUID()).build();

        Page page = Page.builder()
            .id(UUID.randomUUID())
            .title("Guide du aim")
            .slug("guide-aim")
            .author(author)
            .category(category)
            .isPublished(true)
            .build();

        when(categoryRepository.findBySlug("aim")).thenReturn(Optional.of(category));
        when(pageRepository.findByCategoryAndIsPublishedTrueAndTitleContainsIgnoreCase(category, "guide"))
            .thenReturn(List.of(page));

        // Act
        Optional<List<PageSummaryDTO>> result = pageService.searchPagesByCategorySlug("aim", "guide");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals("Guide du aim", result.get().getFirst().title());
    }
}
