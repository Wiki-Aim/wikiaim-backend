package com.wikiaim.backend.issues;

import com.wikiaim.backend.users.User;
import com.wikiaim.backend.users.UserRepository;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import java.util.List;

@Singleton
public class IssueService {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final IssueMapper issueMapper;

    public IssueService(IssueRepository issueRepository, UserRepository userRepository, IssueMapper issueMapper) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.issueMapper = issueMapper;
    }

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
}