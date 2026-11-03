package com.school.dao.impl;

import com.school.dao.DailyReportDao;
import com.school.model.DailyReport;
import com.school.model.ReportRubric;
import com.school.util.MyBatis;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** MyBatis-backed {@link DailyReportDao}; SQL lives in {@code mappers/DailyReportMapper.xml}. */
public class DailyReportDaoImpl implements DailyReportDao {

    @Override
    public long insertReport(DailyReport r) {
        MyBatis.insert("DailyReportMapper.insertReport", r);
        return r.getId();
    }

    @Override
    public void updateReport(DailyReport r) {
        MyBatis.update("DailyReportMapper.updateReport", r);
    }

    @Override
    public void publish(long reportId) {
        MyBatis.update("DailyReportMapper.publish", MyBatis.args("reportId", reportId));
    }

    @Override
    public void unpublish(long reportId) {
        MyBatis.update("DailyReportMapper.unpublish", MyBatis.args("reportId", reportId));
    }

    @Override
    public Optional<DailyReport> findReport(long id) {
        return MyBatis.selectOne("DailyReportMapper.findReport", id);
    }

    @Override
    public boolean dateExists(long studentId, LocalDate date, long excludeId) {
        return MyBatis.count("DailyReportMapper.dateExists", MyBatis.args(
                "studentId", studentId, "date", date, "excludeId", excludeId)) > 0;
    }

    @Override
    public List<DailyReport> reportsForStudent(long studentId, boolean publishedOnly) {
        return MyBatis.selectList("DailyReportMapper.reportsForStudent",
                MyBatis.args("studentId", studentId, "publishedOnly", publishedOnly));
    }

    @Override
    public void insertRubric(ReportRubric r) {
        MyBatis.insert("DailyReportMapper.insertRubric", r);
    }

    @Override
    public void deleteRubricsForReport(long reportId) {
        MyBatis.update("DailyReportMapper.deleteRubricsForReport", MyBatis.args("reportId", reportId));
    }

    @Override
    public List<ReportRubric> rubricsForReport(long reportId) {
        return MyBatis.selectList("DailyReportMapper.rubricsForReport", reportId);
    }

    @Override
    public void acknowledge(long reportId, long parentId) {
        MyBatis.insert("DailyReportMapper.acknowledge",
                MyBatis.args("reportId", reportId, "parentId", parentId));
    }

    @Override
    public Optional<LocalDateTime> acknowledgedAt(long reportId, long parentId) {
        return MyBatis.selectOne("DailyReportMapper.acknowledgedAt",
                MyBatis.args("reportId", reportId, "parentId", parentId));
    }

    @Override
    public Optional<LocalDateTime> latestAcknowledgement(long reportId) {
        return MyBatis.selectOne("DailyReportMapper.latestAcknowledgement",
                MyBatis.args("reportId", reportId));
    }
}