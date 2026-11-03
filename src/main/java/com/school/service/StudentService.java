package com.school.service;

import com.school.dao.AssignmentDao;
import com.school.dao.StudentDao;
import com.school.model.Assignment;
import com.school.model.Student;
import com.school.model.User;
import com.school.util.AccessDeniedException;
import com.school.util.Lookups;
import com.school.util.ValidationException;

import java.util.List;

/**
 * Student profile management plus teacher/parent assignment maintenance. Teacher
 * and parent reads are scoped to their assignments at this layer.
 */
public class StudentService {

    private final StudentDao studentDao;
    private final AssignmentDao assignmentDao;
    private final AccessControl access;

    public StudentService(StudentDao studentDao, AssignmentDao assignmentDao, AccessControl access) {
        this.studentDao = studentDao;
        this.assignmentDao = assignmentDao;
        this.access = access;
    }

    /** The roster visible to a user: full (admin) or assignment-scoped. */
    public List<Student> visibleRoster(User user, String disability, String support,
                                       String classGroup, String keyword) {
        switch (user.getRole()) {
            case "ADMIN":
                return studentDao.search(disability, support, classGroup, keyword, false);
            case "TEACHER":
                return assignmentDao.studentsForTeacher(user.getId());
            case "PARENT":
                return assignmentDao.studentsForParent(user.getId());
            default:
                throw new AccessDeniedException("Unknown role.");
        }
    }

    /** Loads a student, enforcing the caller's access scope. */
    public Student get(User user, long studentId) {
        access.require(user, studentId);
        return studentDao.findById(studentId)
                .orElseThrow(() -> new ValidationException("Student not found."));
    }

    public long create(Student s) {
        validate(s, 0);
        return studentDao.insert(s);
    }

    public void update(Student s) {
        validate(s, s.getId());
        studentDao.update(s);
    }

    public void setActive(long studentId, boolean active) {
        studentDao.setActive(studentId, active);
    }

    // --- assignments (admin only at the controller level) ---

    public List<Assignment> teacherLinks(long studentId) {
        return assignmentDao.teacherLinks(studentId);
    }

    public List<Assignment> parentLinks(long studentId) {
        return assignmentDao.parentLinks(studentId);
    }

    public void assignTeacher(long teacherId, long studentId, boolean primary) {
        if (assignmentDao.isTeacherOfStudent(teacherId, studentId)) {
            throw new ValidationException("That teacher is already assigned to this student.");
        }
        assignmentDao.assignTeacher(teacherId, studentId, primary);
    }

    public void removeTeacherLink(long assignmentId) {
        assignmentDao.removeTeacherLink(assignmentId);
    }

    public void assignParent(long parentId, long studentId, String relationship) {
        if (!Lookups.RELATIONSHIPS.contains(relationship)) {
            throw new ValidationException("Invalid relationship.");
        }
        if (assignmentDao.isParentOfStudent(parentId, studentId)) {
            throw new ValidationException("That parent is already linked to this student.");
        }
        assignmentDao.assignParent(parentId, studentId, relationship);
    }

    public void removeParentLink(long assignmentId) {
        assignmentDao.removeParentLink(assignmentId);
    }

    private void validate(Student s, long excludeId) {
        if (isBlank(s.getStudentCode())) {
            throw new ValidationException("Student code is required.");
        }
        if (isBlank(s.getFullName())) {
            throw new ValidationException("Full name is required.");
        }
        if (s.getDateOfBirth() == null) {
            throw new ValidationException("Date of birth is required.");
        }
        if (!Lookups.GENDERS.contains(s.getGender())) {
            throw new ValidationException("Invalid gender.");
        }
        if (!Lookups.DISABILITY_TYPES.containsKey(s.getDisabilityType())) {
            throw new ValidationException("Invalid disability type.");
        }
        if (!Lookups.SUPPORT_LEVELS.contains(s.getSupportLevel())) {
            throw new ValidationException("Invalid support level.");
        }
        if (s.getEnrollmentDate() == null) {
            throw new ValidationException("Enrollment date is required.");
        }
        if (studentDao.codeExists(s.getStudentCode(), excludeId)) {
            throw new ValidationException("Student code '" + s.getStudentCode() + "' is already in use.");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
