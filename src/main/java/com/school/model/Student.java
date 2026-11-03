package com.school.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** An SNE student record — the central entity all other modules hang off. */
public class Student {

    private long id;
    private String studentCode;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;          // MALE | FEMALE | OTHER
    private String disabilityType;  // see Lookups.DISABILITY_TYPES
    private String supportLevel;    // LOW | MODERATE | HIGH | INTENSIVE
    private LocalDate enrollmentDate;
    private String classGroup;
    private String notes;
    private boolean active = true;
    private LocalDateTime createdAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getDisabilityType() { return disabilityType; }
    public void setDisabilityType(String disabilityType) { this.disabilityType = disabilityType; }

    public String getSupportLevel() { return supportLevel; }
    public void setSupportLevel(String supportLevel) { this.supportLevel = supportLevel; }

    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public String getClassGroup() { return classGroup; }
    public void setClassGroup(String classGroup) { this.classGroup = classGroup; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
