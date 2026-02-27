package com.wikiaim.backend.issues;

import jakarta.inject.Singleton;

@Singleton
public class IssueMapper {
    public IssueResponseDTO toDTO(Issue issue) {
        if (issue == null) return null;
        return new IssueResponseDTO(
            issue.getId(),
            issue.getTitle(),
            issue.getDescription(),
            issue.getStatus(),
            issue.getAuthor() != null ? issue.getAuthor().getId() : null,
            issue.getCreatedAt()
        );
    }
}