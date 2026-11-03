package com.school.dao;

import com.school.model.Conversation;
import com.school.model.Message;

import java.util.List;
import java.util.Optional;

/** Data access for direct messages. */
public interface MessageDao {

    long insert(Message m);

    Optional<Message> find(long id);

    List<Message> inbox(long userId);

    List<Message> sent(long userId);

    long unreadCount(long userId);

    /** Marks a message read — only the recipient may do so (enforced in SQL). */
    void markRead(long messageId, long recipientId);

    /** One summary row per (other participant, student) chat thread for this user. */
    List<Conversation> conversations(long userId);

    /** Every message exchanged between two users about a student, oldest first. */
    List<Message> thread(long userId, long otherId, Long studentId);

    /** Every message exchanged between two users (any student), oldest first. */
    List<Message> threadByUser(long userId, long otherId);

    /** Marks all incoming messages in a thread as read for this user. */
    void markThreadRead(long userId, long otherId, Long studentId);

    /** Marks all incoming messages from one user as read (any student). */
    void markThreadReadByUser(long userId, long otherId);
}
