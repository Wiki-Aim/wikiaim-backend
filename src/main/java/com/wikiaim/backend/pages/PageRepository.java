package com.wikiaim.backend.pages;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import com.wikiaim.backend.categories.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PageRepository extends CrudRepository<Page, UUID> {

    Optional<Page> findBySlugAndIsPublishedTrue(String slug);

    List<Page> findByCategoryAndIsPublishedTrue(Category category);

    List<Page> findByCategoryAndIsPublishedTrueAndTitleContainsIgnoreCase(Category category, String title);
}
