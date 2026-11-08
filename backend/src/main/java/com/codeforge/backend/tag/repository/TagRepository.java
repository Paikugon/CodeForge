package com.codeforge.backend.tag.repository;

import com.codeforge.backend.tag.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TagRepository extends JpaRepository<Tag, Long> {
    @Modifying(flushAutomatically = true)
    @Query(value = "delete from problem_tags where tag_id = :tagId", nativeQuery = true)
    int deleteAssignmentsByTagId(@Param("tagId") Long tagId);
}
