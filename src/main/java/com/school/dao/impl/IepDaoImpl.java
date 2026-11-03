package com.school.dao.impl;

import com.school.dao.IepDao;
import com.school.model.IepGoal;
import com.school.model.IepProgram;
import com.school.util.MyBatis;

import java.util.List;
import java.util.Optional;

/** MyBatis-backed {@link IepDao}; SQL lives in {@code mappers/IepMapper.xml}. */
public class IepDaoImpl implements IepDao {

    @Override
    public long insertProgram(IepProgram p) {
        MyBatis.insert("IepMapper.insertProgram", p);
        return p.getId();
    }

    @Override
    public void updateProgram(IepProgram p) {
        MyBatis.update("IepMapper.updateProgram", p);
    }

    @Override
    public Optional<IepProgram> findProgram(long id) {
        return MyBatis.selectOne("IepMapper.findProgram", id);
    }

    @Override
    public List<IepProgram> programsForStudent(long studentId) {
        return MyBatis.selectList("IepMapper.programsForStudent", studentId);
    }

    @Override
    public Optional<IepProgram> activeProgramForStudent(long studentId) {
        return MyBatis.selectOne("IepMapper.activeProgramForStudent", studentId);
    }

    @Override
    public boolean yearExists(long studentId, String academicYear, long excludeId) {
        return MyBatis.count("IepMapper.yearExists", MyBatis.args(
                "studentId", studentId, "academicYear", academicYear, "excludeId", excludeId)) > 0;
    }

    @Override
    public long insertGoal(IepGoal g) {
        MyBatis.insert("IepMapper.insertGoal", g);
        return g.getId();
    }

    @Override
    public void updateGoal(IepGoal g) {
        MyBatis.update("IepMapper.updateGoal", g);
    }

    @Override
    public Optional<IepGoal> findGoal(long id) {
        return MyBatis.selectOne("IepMapper.findGoal", id);
    }

    @Override
    public List<IepGoal> goalsForProgram(long iepId) {
        return MyBatis.selectList("IepMapper.goalsForProgram", iepId);
    }

    @Override
    public void deleteGoal(long goalId) {
        MyBatis.update("IepMapper.deleteGoal", MyBatis.args("goalId", goalId));
    }

    @Override
    public void insertProgressSnapshot(long goalId, int progressPct, long recordedBy) {
        MyBatis.insert("IepMapper.insertProgressSnapshot", MyBatis.args(
                "goalId", goalId, "progressPct", progressPct, "recordedBy", recordedBy));
    }

}