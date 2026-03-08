package com.wikiaim.backend.revisions;

import com.wikiaim.backend.core.TipTapTextExtractor;
import com.wikiaim.backend.pages.Page;
import com.wikiaim.backend.pages.PageRepository;
import com.wikiaim.backend.users.User;
import com.wikiaim.backend.users.UserRepository;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MicronautTest(environments = "test", startApplication = false)
@Property(name = "micronaut.security.enabled", value = "false")
class RevisionServiceTest {

    @MockBean(PageRevisionRepository.class)
    PageRevisionRepository revisionRepository() {
        return mock(PageRevisionRepository.class);
    }

    @MockBean(PageRepository.class)
    PageRepository pageRepository() {
        return mock(PageRepository.class);
    }

    @MockBean(UserRepository.class)
    UserRepository userRepository() {
        return mock(UserRepository.class);
    }

    @MockBean(TipTapTextExtractor.class)
    TipTapTextExtractor textExtractor() {
        return mock(TipTapTextExtractor.class);
    }

    @Inject
    PageRevisionRepository revisionRepository;

    @Inject
    PageRepository pageRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    TipTapTextExtractor textExtractor;

    @Inject
    RevisionService revisionService;

    @BeforeEach
    void setUp() {
        reset(revisionRepository, pageRepository, userRepository, textExtractor);
    }

    @Test
    void proposeRevision_shouldCreateAndReturnRevision() {
        // Arrange
        UUID pageId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        Page page = Page.builder().id(pageId).build();
        User author = User.builder().id(authorId).build();

        ProposeRevisionDTO dto = new ProposeRevisionDTO(
            pageId, authorId, "Titre proposé", "{\"blocks\":[]}", "feat: nouveau contenu"
        );

        when(pageRepository.findById(pageId)).thenReturn(Optional.of(page));
        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(revisionRepository.save(any(PageRevision.class))).thenAnswer(invocation -> {
            PageRevision saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        // Act
        RevisionResponseDTO result = revisionService.proposeRevision(dto);

        // Assert
        assertNotNull(result);
        assertEquals("Titre proposé", result.proposedTitle());
        assertEquals("feat: nouveau contenu", result.commitMessage());
        assertEquals(RevisionStatus.PENDING, result.status());

        ArgumentCaptor<PageRevision> captor = ArgumentCaptor.forClass(PageRevision.class);
        verify(revisionRepository).save(captor.capture());

        PageRevision captured = captor.getValue();
        assertEquals(page, captured.getPage());
        assertEquals(author, captured.getAuthor());
        assertEquals("{\"blocks\":[]}", captured.getProposedContent());
    }

    @Test
    void proposeRevision_shouldThrowWhenPageNotFound() {
        ProposeRevisionDTO dto = new ProposeRevisionDTO(
            UUID.randomUUID(), UUID.randomUUID(), "Titre", "{}", "msg"
        );

        when(pageRepository.findById(dto.pageId())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> revisionService.proposeRevision(dto)
        );

        assertEquals("Page introuvable", exception.getMessage());
        verify(revisionRepository, never()).save(any());
    }

    @Test
    void proposeRevision_shouldThrowWhenAuthorNotFound() {
        UUID pageId = UUID.randomUUID();
        ProposeRevisionDTO dto = new ProposeRevisionDTO(
            pageId, UUID.randomUUID(), "Titre", "{}", "msg"
        );

        when(pageRepository.findById(pageId)).thenReturn(Optional.of(Page.builder().id(pageId).build()));
        when(userRepository.findById(dto.authorId())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> revisionService.proposeRevision(dto)
        );

        assertEquals("Auteur introuvable", exception.getMessage());
        verify(revisionRepository, never()).save(any());
    }

    @Test
    void getPendingRevisions_shouldReturnMappedRevisions() {
        // Arrange
        Page page = Page.builder().id(UUID.randomUUID()).build();

        PageRevision revision = PageRevision.builder()
            .id(UUID.randomUUID())
            .page(page)
            .proposedTitle("Titre")
            .status(RevisionStatus.PENDING)
            .build();

        when(revisionRepository.findByStatus(RevisionStatus.PENDING)).thenReturn(List.of(revision));

        // Act
        List<RevisionResponseDTO> result = revisionService.getPendingRevisions();

        // Assert
        assertEquals(1, result.size());
        assertEquals(revision.getId(), result.getFirst().id());
        assertEquals(RevisionStatus.PENDING, result.getFirst().status());
    }

    @Test
    void getPendingRevisions_shouldReturnEmptyListWhenNoPending() {
        when(revisionRepository.findByStatus(RevisionStatus.PENDING)).thenReturn(List.of());

        List<RevisionResponseDTO> result = revisionService.getPendingRevisions();

        assertTrue(result.isEmpty());
    }

    @Test
    void approveRevision_shouldApproveAndUpdatePage() {
        // Arrange
        UUID revisionId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();

        Page page = Page.builder()
            .id(UUID.randomUUID())
            .title("Ancien titre")
            .currentContent("{\"old\":true}")
            .build();

        PageRevision revision = PageRevision.builder()
            .id(revisionId)
            .page(page)
            .proposedTitle("Nouveau titre")
            .proposedContent("{\"new\":true}")
            .status(RevisionStatus.PENDING)
            .build();

        User reviewer = User.builder().id(reviewerId).build();

        when(revisionRepository.findById(revisionId)).thenReturn(Optional.of(revision));
        when(userRepository.findById(reviewerId)).thenReturn(Optional.of(reviewer));

        // Act
        revisionService.approveRevision(revisionId, reviewerId);

        // Assert
        assertEquals(RevisionStatus.APPROVED, revision.getStatus());
        assertEquals(reviewer, revision.getReviewer());
        assertNotNull(revision.getReviewedAt());
        verify(revisionRepository).update(revision);

        assertEquals("Nouveau titre", page.getTitle());
        assertEquals("{\"new\":true}", page.getCurrentContent());
        verify(pageRepository).update(page);
    }

    @Test
    void approveRevision_shouldThrowWhenRevisionNotFound() {
        UUID revisionId = UUID.randomUUID();

        when(revisionRepository.findById(revisionId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> revisionService.approveRevision(revisionId, UUID.randomUUID())
        );

        assertEquals("Révision introuvable", exception.getMessage());
        verify(revisionRepository, never()).update(any());
        verify(pageRepository, never()).update(any());
    }

    @Test
    void approveRevision_shouldThrowWhenRevisionNotPending() {
        UUID revisionId = UUID.randomUUID();

        PageRevision revision = PageRevision.builder()
            .id(revisionId)
            .status(RevisionStatus.APPROVED)
            .build();

        when(revisionRepository.findById(revisionId)).thenReturn(Optional.of(revision));

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> revisionService.approveRevision(revisionId, UUID.randomUUID())
        );

        assertEquals("Seule une révision PENDING peut être approuvée.", exception.getMessage());
        verify(revisionRepository, never()).update(any());
        verify(pageRepository, never()).update(any());
    }

    @Test
    void approveRevision_shouldThrowWhenReviewerNotFound() {
        UUID revisionId = UUID.randomUUID();
        UUID reviewerId = UUID.randomUUID();

        Page page = Page.builder().id(UUID.randomUUID()).build();

        PageRevision revision = PageRevision.builder()
            .id(revisionId)
            .page(page)
            .status(RevisionStatus.PENDING)
            .build();

        when(revisionRepository.findById(revisionId)).thenReturn(Optional.of(revision));
        when(userRepository.findById(reviewerId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> revisionService.approveRevision(revisionId, reviewerId)
        );

        assertEquals("Modérateur introuvable", exception.getMessage());
        verify(revisionRepository, never()).update(any());
        verify(pageRepository, never()).update(any());
    }

    @Test
    void getRevisionDiff_shouldReturnDiffWithChanges() {
        // Arrange
        UUID revisionId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();

        String currentContent = "{\"blocks\":[{\"type\":\"paragraph\",\"data\":{\"text\":\"old\"}}]}";
        String proposedContent = "{\"blocks\":[{\"type\":\"paragraph\",\"data\":{\"text\":\"new\"}}]}";

        Page page = Page.builder()
            .id(pageId)
            .title("Titre actuel")
            .currentContent(currentContent)
            .build();

        PageRevision revision = PageRevision.builder()
            .id(revisionId)
            .page(page)
            .proposedTitle("Titre modifié")
            .proposedContent(proposedContent)
            .commitMessage("fix: correction contenu")
            .status(RevisionStatus.PENDING)
            .build();

        when(revisionRepository.findById(revisionId)).thenReturn(Optional.of(revision));
        when(textExtractor.extractLines(currentContent))
            .thenReturn(List.of("Ligne inchangée", "Ligne à supprimer", "Dernière ligne"));
        when(textExtractor.extractLines(proposedContent))
            .thenReturn(List.of("Ligne inchangée", "Ligne ajoutée", "Dernière ligne"));

        // Act
        RevisionDiffDTO result = revisionService.getRevisionDiff(revisionId);

        // Assert
        assertNotNull(result);
        assertEquals(revisionId, result.revisionId());
        assertEquals(pageId, result.pageId());
        assertEquals("Titre actuel", result.currentTitle());
        assertEquals("Titre modifié", result.proposedTitle());
        assertTrue(result.titleChanged());
        assertEquals(RevisionStatus.PENDING, result.status());

        List<DiffLineDTO> diff = result.contentDiff();
        assertFalse(diff.isEmpty());

        assertTrue(diff.stream().anyMatch(d -> d.type() == DiffType.EQUAL && d.content().equals("Ligne inchangée")));
        assertTrue(diff.stream().anyMatch(d -> d.type() == DiffType.DELETE && d.content().equals("Ligne à supprimer")));
        assertTrue(diff.stream().anyMatch(d -> d.type() == DiffType.INSERT && d.content().equals("Ligne ajoutée")));
    }

    @Test
    void getRevisionDiff_shouldThrowWhenRevisionNotFound() {
        UUID revisionId = UUID.randomUUID();

        when(revisionRepository.findById(revisionId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> revisionService.getRevisionDiff(revisionId)
        );

        assertEquals("Révision introuvable", exception.getMessage());
    }
}
