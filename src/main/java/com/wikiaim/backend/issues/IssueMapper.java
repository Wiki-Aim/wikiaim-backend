package com.wikiaim.backend.issues;

import io.micronaut.context.annotation.Mapper;
import io.micronaut.context.annotation.Mapper.Mapping;
import jakarta.inject.Singleton;

@Singleton
public interface IssueMapper {

    @Mapping(to = "authorId", from = "#{issue.author.id}", condition = "#{issue.author != null}")
    @Mapper
    IssueResponseDTO toDTO(Issue issue);
}
