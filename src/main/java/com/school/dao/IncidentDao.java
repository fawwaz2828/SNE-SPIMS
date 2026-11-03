package com.school.dao;

import com.school.model.BehaviorIncident;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Data access for behavioural incident records. */
public interface IncidentDao {

    long insert(BehaviorIncident i);

    Optional<BehaviorIncident> find(long id);

    /** Admin / teacher filtered list. Pass null studentId for school-wide (admin). */
    List<BehaviorIncident> search(Long studentId, String severity, LocalDate from, LocalDate to);

    /** All incidents for the students in a teacher's caseload (filtered). */
    List<BehaviorIncident> forTeacher(long teacherId, String severity, LocalDate from, LocalDate to);

    /** Parent view: only HIGH/CRITICAL or explicitly parent-visible incidents. */
    List<BehaviorIncident> visibleToParentForStudent(long studentId);
}
