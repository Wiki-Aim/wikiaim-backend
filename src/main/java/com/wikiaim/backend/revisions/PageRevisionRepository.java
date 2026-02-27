package com.wikiaim.backend.revisions;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PageRevisionRepository extends CrudRepository<PageRevision, UUID> {

    List<PageRevision> findByStatus(RevisionStatus status);

    List<PageRevision> findByPageIdOrderByCreatedAtDesc(UUID pageId);
}