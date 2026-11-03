package com.school.dao.impl;

import com.school.dao.AnnouncementDao;
import com.school.model.Announcement;
import com.school.util.MyBatis;

import java.util.List;
import java.util.Optional;

/** MyBatis-backed {@link AnnouncementDao}; SQL lives in {@code mappers/AnnouncementMapper.xml}. */
public class AnnouncementDaoImpl implements AnnouncementDao {

    @Override
    public long insert(Announcement a) {
        MyBatis.insert("AnnouncementMapper.insert", a);
        return a.getId();
    }

    @Override
    public void update(Announcement a) {
        MyBatis.update("AnnouncementMapper.update", a);
    }

    @Override
    public void setActive(long id, boolean active) {
        MyBatis.update("AnnouncementMapper.setActive", MyBatis.args("id", id, "active", active));
    }

    @Override
    public void delete(long id) {
        MyBatis.update("AnnouncementMapper.delete", MyBatis.args("id", id));
    }

    @Override
    public Optional<Announcement> find(long id) {
        return MyBatis.selectOne("AnnouncementMapper.find", id);
    }

    @Override
    public List<Announcement> all() {
        return MyBatis.selectList("AnnouncementMapper.all");
    }

    @Override
    public List<Announcement> activeForRole(String role, int limit) {
        return MyBatis.selectList("AnnouncementMapper.activeForRole",
                MyBatis.args("role", role, "limit", limit));
    }
}