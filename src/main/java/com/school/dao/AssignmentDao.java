package com.school.dao;

import com.school.model.Assignment;
import com.school.model.Student;

import java.util.List;

/**
 * Data access for the two relationship tables (teacher_student, parent_student).
 * These links define the data-access scope of teachers and parents, so this DAO
 * also provides the access-check helpers used throughout the Service layer.
 */
public interface AssignmentDao {

    // --- teacher links ---
    long assignTeacher(long teacherId, long studentId, boolean primary);

    void removeTeacherLink(long assignmentId);

    /** Students in a teacher's caseload (active only). */
    List<Student> studentsForTeacher(long teacherId);

    /** Teacher links for one student (for the profile page). */
    List<Assignment> teacherLinks(long studentId);

    boolean isTeacherOfStudent(long teacherId, long studentId);

    // --- parent links ---
    long assignParent(long parentId, long studentId, String relationship);

    void removeParentLink(long assignmentId);

    /** Students (children) linked to a parent. */
    List<Student> studentsForParent(long parentId);

    List<Assignment> parentLinks(long studentId);

    boolean isParentOfStudent(long parentId, long studentId);

    /** The teacher ids assigned to a student — used to route a parent's message. */
    List<Long> teacherIdsForStudent(long studentId);
}
