package com.school.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** An Individualized Education Program for one student in one academic year. */
public class IepProgram {

    private long id;
    private long studentId;
    private String title;
    private String academicYear;
    private LocalDate startDate;
    private LocalDate endDate;
    private String presentLevels;   // baseline narrative (PLAAFP / asesmen awal)
    private String accommodations;  // accommodations & modifications
    private String status;       // DRAFT | ACTIVE | COMPLETED | ARCHIVED
    private long createdBy;
    private LocalDateTime createdAt;

    // Display joins
    private String studentName;
    private String createdByName;
    /** Goals grouped/loaded for the detail view. */
    private List<IepGoal> goals = new ArrayList<>();

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getPresentLevels() { return presentLevels; }
    public void setPresentLevels(String presentLevels) { this.presentLevels = presentLevels; }

    public String getAccommodations() { return accommodations; }
    public void setAccommodations(String accommodations) { this.accommodations = accommodations; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedBy() { return createdBy; }
    public void setCreatedBy(long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public List<IepGoal> getGoals() { return goals; }
    public void setGoals(List<IepGoal> goals) { this.goals = goals; }
}
