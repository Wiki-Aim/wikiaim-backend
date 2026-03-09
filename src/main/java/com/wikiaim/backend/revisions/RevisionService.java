package com.wikiaim.backend.revisions;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import com.wikiaim.backend.core.TipTapTextExtractor;
import com.wikiaim.backend.pages.Page;
import com.wikiaim.backend.pages.PageRepository;
import com.wikiaim.backend.users.Role;
import com.wikiaim.backend.users.User;
import com.wikiaim.backend.users.UserRepository;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor
public class RevisionService {

    private final PageRevisionRepository revisionRepository;
    private final PageRepository pageRepository;
    private final UserRepository userRepository;
    private final RevisionMapper revisionMapper;
    private final TipTapTextExtractor textExtractor;

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

        if (revision.getAuthor().getId().equals(reviewerId) && reviewer.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Un modérateur ne peut pas approuver sa propre révision.");
        }

        revision.setStatus(RevisionStatus.APPROVED);
        revision.setReviewer(reviewer);
        revision.setReviewedAt(Instant.now());
        revisionRepository.update(revision);

        Page page = revision.getPage();
        page.setTitle(revision.getProposedTitle());
        page.setCurrentContent(revision.getProposedContent());
        pageRepository.update(page);
    }

    @Transactional
    public RevisionDiffDTO getRevisionDiff(UUID revisionId) {
        PageRevision revision = revisionRepository.findById(revisionId)
                .orElseThrow(() -> new IllegalArgumentException("Révision introuvable"));

        Page page = revision.getPage();

        List<String> currentLines = textExtractor.extractLines(page.getCurrentContent());
        List<String> proposedLines = textExtractor.extractLines(revision.getProposedContent());

        Patch<String> patch = DiffUtils.diff(currentLines, proposedLines);
        List<DiffLineDTO> diffLines = buildDiffLines(currentLines, patch);

        boolean titleChanged = !page.getTitle().equals(revision.getProposedTitle());

        return new RevisionDiffDTO(
                revision.getId(),
                page.getId(),
                page.getTitle(),
                revision.getProposedTitle(),
                titleChanged,
                diffLines,
                revision.getCommitMessage(),
                revision.getStatus(),
                revision.getCreatedAt()
        );
    }

    private List<DiffLineDTO> buildDiffLines(List<String> originalLines, Patch<String> patch) {
        List<DiffLineDTO> result = new ArrayList<>();
        int currentPos = 0;

        for (AbstractDelta<String> delta : patch.getDeltas()) {
            int deltaStart = delta.getSource().getPosition();

            for (int i = currentPos; i < deltaStart; i++) {
                result.add(new DiffLineDTO(DiffType.EQUAL, originalLines.get(i)));
            }

            for (String line : delta.getSource().getLines()) {
                result.add(new DiffLineDTO(DiffType.DELETE, line));
            }

            for (String line : delta.getTarget().getLines()) {
                result.add(new DiffLineDTO(DiffType.INSERT, line));
            }

            currentPos = deltaStart + delta.getSource().size();
        }

        for (int i = currentPos; i < originalLines.size(); i++) {
            result.add(new DiffLineDTO(DiffType.EQUAL, originalLines.get(i)));
        }

        return result;
    }
}