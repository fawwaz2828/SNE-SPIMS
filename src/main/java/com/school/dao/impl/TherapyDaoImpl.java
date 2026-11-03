package com.school.dao.impl;

import com.school.dao.TherapyDao;
import com.school.model.TherapySession;
import com.school.util.MyBatis;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** MyBatis-backed {@link TherapyDao}; SQL lives in {@code mappers/TherapyMapper.xml}. */
public class TherapyDaoImpl implements TherapyDao {

    @Override
    public long insert(TherapySession s) {
        MyBatis.insert("TherapyMapper.insert", s);
        return s.getId();
    }

    @Override
    public void update(TherapySession s) {
        MyBatis.update("TherapyMapper.update", s);
    }

    @Override
    public void delete(long id) {
        MyBatis.update("TherapyMapper.delete", MyBatis.args("id", id));
    }

    @Override
    public Optional<TherapySession> find(long id) {
        return MyBatis.selectOne("TherapyMapper.find", id);
    }

    @Override
    public List<TherapySession> forStudent(long studentId) {
        return MyBatis.selectList("TherapyMapper.forStudent", studentId);
    }

    @Override
    public List<TherapySession> forTherapist(long therapistId) {
        return MyBatis.selectList("TherapyMapper.forTherapist", therapistId);
    }

    @Override
    public boolean hasConflict(long studentId, LocalDateTime start, LocalDateTime end, long excludeId) {
        return MyBatis.count("TherapyMapper.hasConflict", MyBatis.args(
                "studentId", studentId, "start", start, "end", end, "excludeId", excludeId)) > 0;
    }

    @Override
    public boolean hasTherapistConflict(long therapistId, LocalDateTime start, LocalDateTime end, long excludeId) {
        return MyBatis.count("TherapyMapper.hasTherapistConflict", MyBatis.args(
                "therapistId", therapistId, "start", start, "end", end, "excludeId", excludeId)) > 0;
    }

    @Override
    public boolean hasRoomConflict(String location, LocalDateTime start, LocalDateTime end, long excludeId) {
        return MyBatis.count("TherapyMapper.hasRoomConflict", MyBatis.args(
                "location", location, "start", start, "end", end, "excludeId", excludeId)) > 0;
    }

    @Override
    public List<TherapySession> forStudentsInRange(List<Long> ids, LocalDateTime from, LocalDateTime to) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return MyBatis.selectList("TherapyMapper.forStudentsInRange",
                MyBatis.args("ids", ids, "from", from, "to", to));
    }
}