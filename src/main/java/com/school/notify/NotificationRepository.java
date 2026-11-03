package com.school.notify;

import com.school.model.Notification;
import com.school.model.TherapySession;
import com.school.util.MyBatis;
import java.util.List;
import org.springframework.stereotype.Repository;

/** MyBatis-backed data access for notifications + the therapy-reminder query. */
@Repository
public class NotificationRepository {

    public long insert(Notification n) {
        MyBatis.insert("NotificationMapper.insert", n);
        return n.getId();
    }

    public long unreadCount(long userId) {
        return MyBatis.count("NotificationMapper.unreadCount", MyBatis.args("userId", userId));
    }

    public List<Notification> listForUser(long userId, boolean includeArchived) {
        return MyBatis.selectList("NotificationMapper.listForUser",
                MyBatis.args("userId", userId, "includeArchived", includeArchived));
    }

    public void markRead(long id, long userId) {
        MyBatis.update("NotificationMapper.markRead", MyBatis.args("id", id, "userId", userId));
    }

    public void markAllRead(long userId) {
        MyBatis.update("NotificationMapper.markAllRead", MyBatis.args("userId", userId));
    }

    public void archive(long id, long userId) {
        MyBatis.update("NotificationMapper.archive", MyBatis.args("id", id, "userId", userId));
    }

    public List<TherapySession> upcomingTherapy() {
        return MyBatis.selectList("NotificationMapper.upcomingTherapy");
    }
}
