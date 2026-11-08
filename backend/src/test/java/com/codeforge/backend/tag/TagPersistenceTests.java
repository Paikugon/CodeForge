package com.codeforge.backend.tag;

import com.codeforge.backend.problem.model.Problem;
import com.codeforge.backend.tag.model.Tag;
import jakarta.persistence.EntityManager;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TagPersistenceTests {

    @Autowired
    private EntityManager entityManager;

    @Test
    void problemsWithoutTagsRemainValidAndTagsAreLazy() {
        Problem problem = problem("no-tags");
        entityManager.flush();
        entityManager.clear();

        Problem reloaded = entityManager.find(Problem.class, problem.getId());

        assertFalse(Hibernate.isInitialized(reloaded.getTags()));
        assertTrue(reloaded.getTags().isEmpty());
    }

    @Test
    void aTagCanBeSharedAndTheSameProblemTagPairIsStoredOnce() {
        Tag arrays = tag("Arrays");
        Tag sorting = tag("Sorting");
        Problem first = problem("first");
        Problem second = problem("second");
        first.addTag(arrays);
        first.addTag(sorting);
        first.addTag(arrays);
        second.addTag(arrays);
        entityManager.flush();
        entityManager.clear();

        Problem firstReloaded = entityManager.find(Problem.class, first.getId());
        Problem secondReloaded = entityManager.find(Problem.class, second.getId());
        assertEquals(Set.of(arrays.getId(), sorting.getId()), firstReloaded.getTags().stream()
                .map(Tag::getId).collect(Collectors.toSet()));
        assertEquals(Set.of(arrays.getId()), secondReloaded.getTags().stream()
                .map(Tag::getId).collect(Collectors.toSet()));
        assertEquals(3L, ((Number) entityManager.createNativeQuery(
                "select count(*) from problem_tags").getSingleResult()).longValue());
    }

    @Test
    void removingAnAssignmentPreservesTheTagAndOtherAssignments() {
        Tag arrays = tag("Arrays");
        Problem first = problem("first");
        Problem second = problem("second");
        first.addTag(arrays);
        second.addTag(arrays);
        entityManager.flush();

        first.removeTag(arrays);
        entityManager.flush();
        entityManager.clear();

        assertTrue(entityManager.find(Problem.class, first.getId()).getTags().isEmpty());
        assertEquals(1, entityManager.find(Problem.class, second.getId()).getTags().size());
        assertNotNull(entityManager.find(Tag.class, arrays.getId()));
    }

    @Test
    void deletingAProblemDoesNotDeleteSharedTagsOrOtherProblems() {
        Tag arrays = tag("Arrays");
        Problem first = problem("first");
        Problem second = problem("second");
        first.addTag(arrays);
        second.addTag(arrays);
        entityManager.flush();

        entityManager.remove(first);
        entityManager.flush();
        entityManager.clear();

        assertNull(entityManager.find(Problem.class, first.getId()));
        assertNotNull(entityManager.find(Tag.class, arrays.getId()));
        assertEquals(1, entityManager.find(Problem.class, second.getId()).getTags().size());
        assertEquals(1L, ((Number) entityManager.createNativeQuery(
                "select count(*) from problem_tags").getSingleResult()).longValue());
    }

    @Test
    void generatedIdsAndLazyProxiesDoNotBreakSetMembership() {
        Tag tag = new Tag("Arrays");
        Set<Tag> tags = new LinkedHashSet<>();
        tags.add(tag);
        int hashBeforePersist = tag.hashCode();
        entityManager.persist(tag);
        entityManager.flush();
        entityManager.clear();
        Tag proxy = entityManager.getReference(Tag.class, tag.getId());

        assertFalse(Hibernate.isInitialized(proxy));
        assertEquals(hashBeforePersist, tag.hashCode());
        assertEquals(tag, proxy);
        assertEquals(proxy, tag);
        assertTrue(tags.contains(proxy));
        assertFalse(Hibernate.isInitialized(proxy));
    }

    @Test
    void distinctUnsavedTagsDoNotCompareEqual() {
        Tag arrays = new Tag("Arrays");
        Tag graphs = new Tag("Graphs");

        assertNotEquals(arrays, graphs);
        assertEquals(2, new LinkedHashSet<>(java.util.List.of(arrays, graphs)).size());
    }

    private Tag tag(String name) {
        Tag tag = new Tag(name);
        entityManager.persist(tag);
        return tag;
    }

    private Problem problem(String slug) {
        Problem problem = new Problem("Tag test", "tag-test-" + slug, "A test problem", 1000, 256);
        entityManager.persist(problem);
        return problem;
    }
}
