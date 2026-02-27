package com.wikiaim.backend.revisions;

import com.wikiaim.backend.pages.Page;
import com.wikiaim.backend.users.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "page_revisions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id")
    private Page page;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Column(name = "proposed_title", nullable = false)
    private String proposedTitle;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "proposed_content", columnDefinition = "jsonb", nullable = false)
    private String proposedContent;

    @Column(name = "commit_message", columnDefinition = "TEXT")
    private String commitMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RevisionStatus status = RevisionStatus.PENDING;

    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;
}