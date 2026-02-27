package com.wikiaim.backend.issues;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;
import java.util.UUID;

@Repository
public interface IssueRepository extends CrudRepository<Issue, UUID> {
    List<Issue> findByStatus(IssueStatus status);
}