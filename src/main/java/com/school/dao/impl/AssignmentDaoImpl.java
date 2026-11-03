package com.school.dao.impl;

import com.school.dao.AssignmentDao;
import com.school.model.Assignment;
import com.school.model.Student;
import com.school.util.MyBatis;

import java.util.List;
import java.util.Map;

/** MyBatis-backed {@link AssignmentDao}; SQL lives in {@code mappers/AssignmentMapper.xml}. */
public class AssignmentDaoImpl implements AssignmentDao {

    @Override
    public long assignTeacher(long teacherId, long studentId, boolean primary) {
        Map<String, Object> args = MyBatis.args(
                "teacherId", teacherId, "studentId", studentId, "primary", primary);
        MyBatis.insert("AssignmentMapper.assignTeacher", args);
        return (Long) args.get("id");
    }

    @Override
    public void removeTeacherLink(long assignmentId) {
        MyBatis.update("AssignmentMapper.removeTeacherLink", MyBatis.args("assignmentId", assignmentId));
    }

    @Override
    public List<Student> studentsForTeacher(long teacherId) {
        return MyBatis.selectList("AssignmentMapper.studentsForTeacher", teacherId);
    }

    @Override
    public List<Assignment> teacherLinks(long studentId) {
        return MyBatis.selectList("AssignmentMapper.teacherLinks", studentId);
    }

    @Override
    public boolean isTeacherOfStudent(long teacherId, long studentId) {
        return MyBatis.count("AssignmentMapper.isTeacherOfStudent",
                MyBatis.args("teacherId", teacherId, "studentId", studentId)) > 0;
    }

    @Override
    public long assignParent(long parentId, long studentId, String relationship) {
        Map<String, Object> args = MyBatis.args(
                "parentId", parentId, "studentId", studentId, "relationship", relationship);
        MyBatis.insert("AssignmentMapper.assignParent", args);
        return (Long) args.get("id");
    }

    @Override
    public void removeParentLink(long assignmentId) {
        MyBatis.update("AssignmentMapper.removeParentLink", MyBatis.args("assignmentId", assignmentId));
    }

    @Override
    public List<Student> studentsForParent(long parentId) {
        return MyBatis.selectList("AssignmentMapper.studentsForParent", parentId);
    }

    @Override
    public List<Assignment> parentLinks(long studentId) {
        return MyBatis.selectList("AssignmentMapper.parentLinks", studentId);
    }

    @Override
    public boolean isParentOfStudent(long parentId, long studentId) {
        return MyBatis.count("AssignmentMapper.isParentOfStudent",
                MyBatis.args("parentId", parentId, "studentId", studentId)) > 0;
    }

    @Override
    public List<Long> teacherIdsForStudent(long studentId) {
        return MyBatis.selectList("AssignmentMapper.teacherIdsForStudent", studentId);
    }
}