package com.school.dao.impl;

import com.school.dao.StudentDao;
import com.school.model.Student;
import com.school.util.MyBatis;

import java.util.List;
import java.util.Optional;

/** MyBatis-backed {@link StudentDao}; SQL lives in {@code mappers/StudentMapper.xml}. */
public class StudentDaoImpl implements StudentDao {

    @Override
    public Optional<Student> findById(long id) {
        return MyBatis.selectOne("StudentMapper.findById", id);
    }

    @Override
    public List<Student> search(String disabilityType, String supportLevel, String classGroup,
                                String keyword, boolean includeInactive) {
        return MyBatis.selectList("StudentMapper.search", MyBatis.args(
                "disabilityType", disabilityType,
                "supportLevel", supportLevel,
                "classGroup", classGroup,
                "keyword", keyword == null ? null : keyword.trim(),
                "includeInactive", includeInactive));
    }

    @Override
    public long insert(Student s) {
        MyBatis.insert("StudentMapper.insert", s);
        return s.getId();
    }

    @Override
    public void update(Student s) {
        MyBatis.update("StudentMapper.update", s);
    }

    @Override
    public void setActive(long studentId, boolean active) {
        MyBatis.update("StudentMapper.setActive",
                MyBatis.args("studentId", studentId, "active", active));
    }

    @Override
    public boolean codeExists(String studentCode, long excludeId) {
        return MyBatis.count("StudentMapper.codeExists",
                MyBatis.args("studentCode", studentCode, "excludeId", excludeId)) > 0;
    }
}