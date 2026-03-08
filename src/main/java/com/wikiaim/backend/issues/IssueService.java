package com.wikiaim.backend.issues;

import com.wikiaim.backend.users.User;
import com.wikiaim.backend.users.UserRepository;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor
public class IssueService {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final IssueMapper issueMapper;

    @Transactional
    public IssueResponseDTO createIssue(CreateIssueDTO dto) {
        User author = userRepository.findById(dto.authorId())
                                    .orElseThrow(() -> new IllegalArgumentException("Auteur introuvable"));

        Issue issue = Issue.builder()
            .title(dto.title())
            .description(dto.description())
            .author(author)
            .status(IssueStatus.OPEN)
            .build();

        return issueMapper.toDTO(issueRepository.save(issue));
    }

    public List<IssueResponseDTO> getOpenIssues() {
        return issueRepository.findByStatus(IssueStatus.OPEN)
                              .stream()
                              .map(issueMapper::toDTO)
                              .toList();
    }

    @Transactional
    public IssueResponseDTO updateStatus(UUID id, UpdateIssueStatusDTO dto) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Issue introuvable"));

        IssueStatus newStatus = IssueStatus.valueOf(dto.status());

        if (issue.getStatus() == newStatus) {
            throw new IllegalStateException("L'issue est déjà au statut " + newStatus);
        }

        issue.setStatus(newStatus);
        return issueMapper.toDTO(issueRepository.update(issue));
    }
}