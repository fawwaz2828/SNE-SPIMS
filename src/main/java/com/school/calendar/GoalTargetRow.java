package com.school.calendar;

import java.time.LocalDate;

/** An IEP goal target date, with its student, for the calendar. */
public class GoalTargetRow {
    private long goalId;
    private String description;
    private String domain;
    private LocalDate targetDate;
    private long studentId;
    private String studentName;

    public long getGoalId() { return goalId; }
    public void setGoalId(long goalId) { this.goalId = goalId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }

    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
}
