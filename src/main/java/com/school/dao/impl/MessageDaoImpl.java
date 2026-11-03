package com.school.dao.impl;

import com.school.dao.MessageDao;
import com.school.model.Conversation;
import com.school.model.Message;
import com.school.util.MyBatis;

import java.util.List;
import java.util.Optional;

/** MyBatis-backed {@link MessageDao}; SQL lives in {@code mappers/MessageMapper.xml}. */
public class MessageDaoImpl implements MessageDao {

    @Override
    public long insert(Message m) {
        MyBatis.insert("MessageMapper.insert", m);
        return m.getId();
    }

    @Override
    public Optional<Message> find(long id) {
        return MyBatis.selectOne("MessageMapper.find", id);
    }

    @Override
    public List<Message> inbox(long userId) {
        return MyBatis.selectList("MessageMapper.inbox", MyBatis.args("userId", userId));
    }

    @Override
    public List<Message> sent(long userId) {
        return MyBatis.selectList("MessageMapper.sent", MyBatis.args("userId", userId));
    }

    @Override
    public long unreadCount(long userId) {
        return MyBatis.count("MessageMapper.unreadCount", MyBatis.args("userId", userId));
    }

    @Override
    public void markRead(long messageId, long recipientId) {
        MyBatis.update("MessageMapper.markRead",
                MyBatis.args("messageId", messageId, "recipientId", recipientId));
    }

    @Override
    public List<Conversation> conversations(long userId) {
        return MyBatis.selectList("MessageMapper.conversations", MyBatis.args("userId", userId));
    }

    @Override
    public List<Message> thread(long userId, long otherId, Long studentId) {
        return MyBatis.selectList("MessageMapper.thread",
                MyBatis.args("userId", userId, "otherId", otherId, "studentId", studentId));
    }

    @Override
    public void markThreadRead(long userId, long otherId, Long studentId) {
        MyBatis.update("MessageMapper.markThreadRead",
                MyBatis.args("userId", userId, "otherId", otherId, "studentId", studentId));
    }

    @Override
    public List<Message> threadByUser(long userId, long otherId) {
        return MyBatis.selectList("MessageMapper.threadByUser",
                MyBatis.args("userId", userId, "otherId", otherId));
    }

    @Override
    public void markThreadReadByUser(long userId, long otherId) {
        MyBatis.update("MessageMapper.markThreadReadByUser",
                MyBatis.args("userId", userId, "otherId", otherId));
    }
}