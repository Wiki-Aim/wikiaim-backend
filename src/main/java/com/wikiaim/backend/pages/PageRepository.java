package com.wikiaim.backend.pages;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PageRepository extends CrudRepository<Page, UUID> {

    List<Page> findByIsPublishedTrue();

    Optional<Page> findBySlugAndIsPublishedTrue(String slug);
}
