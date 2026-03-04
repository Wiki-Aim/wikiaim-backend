package com.wikiaim.backend.revisions;

import io.micronaut.context.annotation.Mapper;
import io.micronaut.context.annotation.Mapper.Mapping;
import jakarta.inject.Singleton;

@Singleton
public interface RevisionMapper {

    @Mapping(to = "pageId", from = "#{pageRevision.page.id}")
    @Mapper
    RevisionResponseDTO toDTO(PageRevision pageRevision);
}
