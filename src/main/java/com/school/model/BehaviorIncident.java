package com.school.model;

import java.time.LocalDateTime;

/** A logged behavioural incident with severity and parent-visibility control. */
public class BehaviorIncident {

    private long id;
    private long studentId;
    private long reportedBy;
    private LocalDateTime incidentDate;
    private String location;
    private String description;
    private String severity;       // LOW | MEDIUM | HIGH | CRITICAL
    private String actionTaken;
    private boolean parentVisible;
    private LocalDateTime createdAt;

    // Display joins
    private String studentName;
    private String reporterName;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getStudentId() { return studentId; }
    public void setStudentId(long studentId) { this.studentId = studentId; }

    public long getReportedBy() { return reportedBy; }
    public void setReportedBy(long reportedBy) { this.reportedBy = reportedBy; }

    public LocalDateTime getIncidentDate() { return incidentDate; }
    public void setIncidentDate(LocalDateTime incidentDate) { this.incidentDate = incidentDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getActionTaken() { return actionTaken; }
    public void setActionTaken(String actionTaken) { this.actionTaken = actionTaken; }

    public boolean isParentVisible() { return parentVisible; }
    public void setParentVisible(boolean parentVisible) { this.parentVisible = parentVisible; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }
}
