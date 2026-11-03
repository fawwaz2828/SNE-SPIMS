package com.school.dao;

import com.school.model.Student;

import java.util.List;
import java.util.Optional;

/** Data access contract for student records. */
public interface StudentDao {

    Optional<Student> findById(long id);

    /**
     * Roster query with optional filters. Any null/empty filter is ignored.
     * Only active students unless {@code includeInactive} is true.
     */
    List<Student> search(String disabilityType, String supportLevel, String classGroup,
                         String keyword, boolean includeInactive);

    long insert(Student s);

    void update(Student s);

    /** Soft-delete: flips the active flag, preserving history. */
    void setActive(long studentId, boolean active);

    boolean codeExists(String studentCode, long excludeId);
}
