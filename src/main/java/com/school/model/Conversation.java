package com.school.model;

import java.time.LocalDateTime;

/**
 * A chat conversation summary: one row per (other participant, student) pair,
 * carrying the most recent message and the caller's unread count. Built by the
 * {@code conversations} query and used to render the rooms list.
 */
public class Conversation {

    private long otherId;
    private String otherName;
    private String otherRole;
    private Long studentId;        // nullable
    private String studentName;
    private String lastBody;
    private LocalDateTime lastAt;
    private long lastSenderId;
    private long unread;

    public long getOtherId() { return otherId; }
    public void setOtherId(long otherId) { this.otherId = otherId; }

    public String getOtherName() { return otherName; }
    public void setOtherName(String otherName) { this.otherName = otherName; }

    public String getOtherRole() { return otherRole; }
    public void setOtherRole(String otherRole) { this.otherRole = otherRole; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getLastBody() { return lastBody; }
    public void setLastBody(String lastBody) { this.lastBody = lastBody; }

    public LocalDateTime getLastAt() { return lastAt; }
    public void setLastAt(LocalDateTime lastAt) { this.lastAt = lastAt; }

    public long getLastSenderId() { return lastSenderId; }
    public void setLastSenderId(long lastSenderId) { this.lastSenderId = lastSenderId; }

    public long getUnread() { return unread; }
    public void setUnread(long unread) { this.unread = unread; }
}
