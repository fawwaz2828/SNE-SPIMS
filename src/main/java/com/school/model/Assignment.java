package com.school.model;

import java.time.LocalDateTime;

/**
 * A teacher-student or parent-student link. Reused for both relationship tables;
 * {@code relationship} is only populated for parent links, {@code primary} only
 * for teacher links. These rows define the data-access scope of teachers and
 * parents throughout the system.
 */
public class Assignment {

    private long id;
    private long userId;            // teacher_id or parent_id
    private long studentId;
    private String relationship;    // parent only: MOTHER | FATHER | GUARDIAN | OTHER
    private boolean primary;        // teacher only
    private LocalDateTime assignedAt;

    // Display joins
    private String userName;
    private String userRole;
    private String studentName;
    private String studentCode;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public boolean isPrimary() { return primary; }
    public void setPrimary(boolean primary) { this.primary = primary; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
}
