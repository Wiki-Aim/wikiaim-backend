package com.wikiaim.backend.revisions;

import com.wikiaim.backend.pages.Page;
import com.wikiaim.backend.pages.PageRepository;
import com.wikiaim.backend.users.User;
import com.wikiaim.backend.users.UserRepository;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Singleton
public class RevisionService {

    private final PageRevisionRepository revisionRepository;
    private final PageRepository pageRepository;
    private final UserRepository userRepository;
    private final RevisionMapper revisionMapper;

    public RevisionService(PageRevisionRepository revisionRepository,
                           PageRepository pageRepository,
                           UserRepository userRepository,
                           RevisionMapper revisionMapper) {
        this.revisionRepository = revisionRepository;
        this.pageRepository = pageRepository;
        this.userRepository = userRepository;
        this.revisionMapper = revisionMapper;
    }

    @Transactional
    public RevisionResponseDTO proposeRevision(ProposeRevisionDTO dto) {
        Page page = pageRepository.findById(dto.pageId())
                                  .orElseThrow(() -> new IllegalArgumentException("Page introuvable"));

        User author = userRepository.findById(dto.authorId())
                                    .orElseThrow(() -> new IllegalArgumentException("Auteur introuvable"));

        PageRevision revision = PageRevision.builder()
            .page(page)
            .author(author)
            .proposedTitle(dto.proposedTitle())
            .proposedContent(dto.proposedContent())
            .commitMessage(dto.commitMessage())
            .status(RevisionStatus.PENDING)
            .build();

        PageRevision savedRevision = revisionRepository.save(revision);
        return revisionMapper.toDTO(savedRevision);
    }

    public List<RevisionResponseDTO> getPendingRevisions() {
        return revisionRepository.findByStatus(RevisionStatus.PENDING)
                                 .stream()
                                 .map(revisionMapper::toDTO)
                                 .toList();
    }

    @Transactional
    public void approveRevision(UUID revisionId, UUID reviewerId) {
        PageRevision revision = revisionRepository.findById(revisionId)
                                                  .orElseThrow(() -> new IllegalArgumentException("Révision introuvable"));

        if (revision.getStatus() != RevisionStatus.PENDING) {
            throw new IllegalStateException("Seule une révision PENDING peut être approuvée.");
        }

        User reviewer = userRepository.findById(reviewerId)
                                      .orElseThrow(() -> new IllegalArgumentException("Modérateur introuvable"));

        revision.setStatus(RevisionStatus.APPROVED);
        revision.setReviewer(reviewer);
        revision.setReviewedAt(Instant.now());
        revisionRepository.update(revision);

        Page page = revision.getPage();
        page.setTitle(revision.getProposedTitle());
        page.setCurrentContent(revision.getProposedContent());
        pageRepository.update(page);
    }
}