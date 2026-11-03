package com.school.model;

import java.time.LocalDateTime;

/** A scheduled therapy session for a student (conflict-checked on save). */
public class TherapySession {

    private long id;
    private long studentId;
    private long therapistId;
    private String therapyType;    // SPEECH | OCCUPATIONAL | PHYSICAL | BEHAVIORAL | COUNSELING
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String status;         // SCHEDULED | COMPLETED | CANCELLED | NO_SHOW
    private String notes;
    private LocalDateTime createdAt;

    // Display joins
    private String studentName;
    private String therapistName;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }

    public long getTherapistId() { return therapistId; }
    public void setTherapistId(long therapistId) { this.therapistId = therapistId; }

    public String getTherapyType() { return therapyType; }
    public void setTherapyType(String therapyType) { this.therapyType = therapyType; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getTherapistName() { return therapistName; }
    public void setTherapistName(String therapistName) { this.therapistName = therapistName; }
}
