package com.school.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** A teacher's daily progress report for a student, with rubric scores. */
public class DailyReport {

    private long id;
    private long studentId;
    private long teacherId;
    private LocalDate reportDate;
    private String summary;
    private String mood;         // HAPPY | CALM | ANXIOUS | FRUSTRATED | TIRED
    private String status;       // DRAFT | PUBLISHED
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;

    // Display joins
    private String studentName;
    private String teacherName;
    private List<ReportRubric> rubrics = new ArrayList<>();

    // Acknowledgement (display only): for a parent = their own ack; for staff = latest parent ack.
    private boolean acknowledged;
    private LocalDateTime acknowledgedAt;

    public boolean isPublished() { return "PUBLISHED".equals(status); }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }

    public long getTeacherId() { return teacherId; }
    public void setTeacherId(long teacherId) { this.teacherId = teacherId; }

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public List<ReportRubric> getRubrics() { return rubrics; }
    public void setRubrics(List<ReportRubric> rubrics) { this.rubrics = rubrics; }

    public boolean isAcknowledged() { return acknowledged; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }

    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
}
