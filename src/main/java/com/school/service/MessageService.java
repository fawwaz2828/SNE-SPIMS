package com.school.service;

import com.school.dao.AssignmentDao;
import com.school.dao.MessageDao;
import com.school.model.Assignment;
import com.school.model.Conversation;
import com.school.model.Message;
import com.school.model.User;
import com.school.util.AccessDeniedException;
import com.school.util.ValidationException;

import java.util.List;

/**
 * Parent-teacher messaging. Every message is anchored to a student. A teacher may
 * message the parents of their assigned students; a parent may message their
 * children's teachers. Recipients are derived from assignment relationships, so a
 * user can only message valid contacts.
 */
public class MessageService {

    private final MessageDao messageDao;
    private final AssignmentDao assignmentDao;
    private final AccessControl access;

    public MessageService(MessageDao messageDao, AssignmentDao assignmentDao, AccessControl access) {
        this.messageDao = messageDao;
        this.assignmentDao = assignmentDao;
        this.access = access;
    }

    public List<Message> inbox(User user) {
        return messageDao.inbox(user.getId());
    }

    public List<Message> sent(User user) {
        return messageDao.sent(user.getId());
    }

    public long unreadCount(User user) {
        return messageDao.unreadCount(user.getId());
    }

    /** The caller's chat threads (rooms), most recently active first. */
    public List<Conversation> conversations(User user) {
        return messageDao.conversations(user.getId());
    }

    /**
     * Loads the full chat thread between the caller and {@code otherId} about
     * {@code studentId}, oldest first, and marks the caller's incoming messages
     * read. The caller must have access to the student.
     */
    public List<Message> thread(User user, long otherId, Long studentId) {
        if (studentId != null) {
            access.require(user, studentId);
        }
        messageDao.markThreadRead(user.getId(), otherId, studentId);
        return messageDao.thread(user.getId(), otherId, studentId);
    }

    /**
     * Merged thread with another user across all students (parent view): a parent
     * chats with a teacher as a person, not once per child. Marks all incoming
     * messages from that user read.
     */
    public List<Message> threadWith(User user, long otherId) {
        messageDao.markThreadReadByUser(user.getId(), otherId);
        return messageDao.threadByUser(user.getId(), otherId);
    }

    /**
     * Sends a chat message to {@code otherId} about {@code studentId}. Unlike the
     * email-style {@link #send}, no subject is required — one is derived from the
     * student context so existing list/search code keeps working.
     */
    public long sendChat(User sender, long otherId, long studentId, String body) {
        String subject = "Chat";
        return send(sender, studentId, otherId, subject, body);
    }

    /** Loads a message; only sender or recipient may read it; marks read for the recipient. */
    public Message open(User user, long messageId) {
        Message m = messageDao.find(messageId)
                .orElseThrow(() -> new ValidationException("Message not found."));
        if (m.getSenderId() != user.getId() && m.getRecipientId() != user.getId()) {
            throw new AccessDeniedException("You cannot view this message.");
        }
        if (m.getRecipientId() == user.getId() && !m.isRead()) {
            messageDao.markRead(messageId, user.getId());
            m.setRead(true);
        }
        return m;
    }

    /** Whether {@code user} may currently message {@code otherId} about {@code studentId}. */
    public boolean canMessage(User user, long otherId, Long studentId) {
        if (studentId == null) {
            return false;
        }
        try {
            return allowedRecipients(user, studentId).stream()
                    .anyMatch(a -> a.getUserId() == otherId);
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Valid recipients for a message about {@code studentId} given the sender's role. */
    public List<Assignment> allowedRecipients(User sender, long studentId) {
        access.require(sender, studentId);
        if (sender.isTeacher()) {
            return assignmentDao.parentLinks(studentId);
        }
        if (sender.isParent()) {
            return assignmentDao.teacherLinks(studentId);
        }
        throw new AccessDeniedException("Administrators do not use direct messaging.");
    }

    public long send(User sender, long studentId, long recipientId, String subject, String body) {
        if (isBlank(subject)) {
            throw new ValidationException("Subject is required.");
        }
        if (isBlank(body)) {
            throw new ValidationException("Message body is required.");
        }
        boolean ok = allowedRecipients(sender, studentId).stream()
                .anyMatch(a -> a.getUserId() == recipientId);
        if (!ok) {
            throw new AccessDeniedException("You may not message that recipient about this student.");
        }
        Message m = new Message();
        m.setSenderId(sender.getId());
        m.setRecipientId(recipientId);
        m.setStudentId(studentId);
        m.setSubject(subject.trim());
        m.setBody(body.trim());
        return messageDao.insert(m);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
