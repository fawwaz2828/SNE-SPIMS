package com.school.model;

import java.time.LocalDateTime;

/** A direct message between two users, optionally anchored to a student. */
public class Message {

    private long id;
    private long senderId;
    private long recipientId;
    private Long studentId;        // nullable
    private String subject;
    private String body;
    private boolean read;
    private LocalDateTime sentAt;

    // Display joins
    private String senderName;
    private String recipientName;
    private String studentName;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getSenderId() { return senderId; }
    public void setSenderId(long senderId) { this.senderId = senderId; }

    public long getRecipientId() { return recipientId; }
    public void setRecipientId(long recipientId) { this.recipientId = recipientId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
}
