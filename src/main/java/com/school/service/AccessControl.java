package com.school.service;

import com.school.dao.AssignmentDao;
import com.school.model.User;
import com.school.util.AccessDeniedException;

/**
 * Central authority for "can this user touch this student?" decisions. Every
 * service that exposes student-scoped data routes through here so the access
 * rules are enforced at the Service layer, not merely hidden in the UI.
 *
 * <ul>
 *   <li>ADMIN  — every student.</li>
 *   <li>TEACHER — only students in their caseload (teacher_student).</li>
 *   <li>PARENT — only their own children (parent_student).</li>
 * </ul>
 */
public class AccessControl {

    private final AssignmentDao assignments;

    public AccessControl(AssignmentDao assignments) {
        this.assignments = assignments;
    }

    /** True if the user may read/act on the given student under their role. */
    public boolean canAccess(User user, long studentId) {
        if (user == null) {
            return false;
        }
        switch (user.getRole()) {
            case "ADMIN":
                return true;
            case "TEACHER":
                return assignments.isTeacherOfStudent(user.getId(), studentId);
            case "PARENT":
                return assignments.isParentOfStudent(user.getId(), studentId);
            default:
                return false;
        }
    }

    /** Enforcing variant — throws {@link AccessDeniedException} when not allowed. */
    public void require(User user, long studentId) {
        if (!canAccess(user, studentId)) {
            throw new AccessDeniedException("You do not have access to this student's records.");
        }
    }
}
