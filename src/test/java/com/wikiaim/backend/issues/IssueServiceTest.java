package com.wikiaim.backend.issues;

import com.wikiaim.backend.users.User;
import com.wikiaim.backend.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private UserRepository userRepository;

    private final IssueMapper issueMapper = new IssueMapper();

    private IssueService issueService;

    @BeforeEach
    void setUp() {
        issueService = new IssueService(issueRepository, userRepository, issueMapper);
    }

    @Test
    void createIssue_shouldCreateAndReturnIssue() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        User author = User.builder().id(authorId).build();

        CreateIssueDTO dto = new CreateIssueDTO("Bug d'affichage", "Le menu ne s'affiche pas", authorId);

        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        // Act
        IssueResponseDTO result = issueService.createIssue(dto);

        // Assert
        assertNotNull(result);
        assertEquals("Bug d'affichage", result.title());
        assertEquals("Le menu ne s'affiche pas", result.description());
        assertEquals(IssueStatus.OPEN, result.status());
        assertEquals(authorId, result.authorId());

        ArgumentCaptor<Issue> captor = ArgumentCaptor.forClass(Issue.class);
        verify(issueRepository).save(captor.capture());

        Issue captured = captor.getValue();
        assertEquals(author, captured.getAuthor());
        assertEquals(IssueStatus.OPEN, captured.getStatus());
    }

    @Test
    void createIssue_shouldThrowWhenAuthorNotFound() {
        // Arrange
        CreateIssueDTO dto = new CreateIssueDTO("Titre", "Description", UUID.randomUUID());

        when(userRepository.findById(dto.authorId())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> issueService.createIssue(dto)
        );

        assertEquals("Auteur introuvable", exception.getMessage());
        verify(issueRepository, never()).save(any());
    }

    @Test
    void getOpenIssues_shouldReturnMappedIssues() {
        // Arrange
        User author = User.builder().id(UUID.randomUUID()).build();

        Issue issue = Issue.builder()
            .id(UUID.randomUUID())
            .title("Bug")
            .description("Description")
            .status(IssueStatus.OPEN)
            .author(author)
            .build();

        when(issueRepository.findByStatus(IssueStatus.OPEN)).thenReturn(List.of(issue));

        // Act
        List<IssueResponseDTO> result = issueService.getOpenIssues();

        // Assert
        assertEquals(1, result.size());
        assertEquals(issue.getId(), result.getFirst().id());
        assertEquals(IssueStatus.OPEN, result.getFirst().status());
    }

    @Test
    void getOpenIssues_shouldReturnEmptyListWhenNoOpenIssues() {
        // Arrange
        when(issueRepository.findByStatus(IssueStatus.OPEN)).thenReturn(List.of());

        // Act
        List<IssueResponseDTO> result = issueService.getOpenIssues();

        // Assert
        assertTrue(result.isEmpty());
    }
}
