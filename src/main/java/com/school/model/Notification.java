package com.school.model;

import java.time.LocalDateTime;

/** A per-user notification raised by an application event or scheduled job. */
public class Notification {

    private long id;
    private long userId;
    private String type;        // DAILY_REPORT | INCIDENT | THERAPY_REMINDER | MESSAGE | GENERAL
    private String title;
    private String message;
    private String link;
    private Long studentId;     // optional context
    private boolean read;       // maps is_read
    private boolean archived;   // maps is_archived
    private LocalDateTime createdAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
