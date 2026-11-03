package com.school.dao.impl;

import com.school.dao.IncidentDao;
import com.school.model.BehaviorIncident;
import com.school.util.MyBatis;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** MyBatis-backed {@link IncidentDao}; SQL lives in {@code mappers/IncidentMapper.xml}. */
public class IncidentDaoImpl implements IncidentDao {

    @Override
    public long insert(BehaviorIncident i) {
        MyBatis.insert("IncidentMapper.insert", i);
        return i.getId();
    }

    @Override
    public Optional<BehaviorIncident> find(long id) {
        return MyBatis.selectOne("IncidentMapper.find", id);
    }

    @Override
    public List<BehaviorIncident> search(Long studentId, String severity, LocalDate from, LocalDate to) {
        Map<String, Object> args = filters(severity, from, to);
        args.put("studentId", studentId);
        return MyBatis.selectList("IncidentMapper.search", args);
    }

    @Override
    public List<BehaviorIncident> forTeacher(long teacherId, String severity, LocalDate from, LocalDate to) {
        Map<String, Object> args = filters(severity, from, to);
        args.put("teacherId", teacherId);
        return MyBatis.selectList("IncidentMapper.forTeacher", args);
    }

    @Override
    public List<BehaviorIncident> visibleToParentForStudent(long studentId) {
        return MyBatis.selectList("IncidentMapper.visibleToParentForStudent", studentId);
    }

    /** Shared optional filters; date bounds become an inclusive-start / exclusive-end datetime range. */
    private static Map<String, Object> filters(String severity, LocalDate from, LocalDate to) {
        LocalDateTime fromTs = from == null ? null : from.atStartOfDay();
        LocalDateTime toTs = to == null ? null : to.plusDays(1).atStartOfDay();
        return MyBatis.args("severity", severity, "from", fromTs, "to", toTs);
    }
}