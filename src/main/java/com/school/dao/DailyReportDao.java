package com.school.dao;

import com.school.model.DailyReport;
import com.school.model.ReportRubric;

import java.util.List;
import java.util.Optional;

/** Data access for daily progress reports and their rubric rows. */
public interface DailyReportDao {

    long insertReport(DailyReport r);

    void updateReport(DailyReport r);

    void publish(long reportId);

    void unpublish(long reportId);

    Optional<DailyReport> findReport(long id);

    boolean dateExists(long studentId, java.time.LocalDate date, long excludeId);

    /** Reports for a student. If {@code publishedOnly}, only PUBLISHED rows (parent view). */
    List<DailyReport> reportsForStudent(long studentId, boolean publishedOnly);

    // rubric rows
    void insertRubric(ReportRubric r);

    void deleteRubricsForReport(long reportId);

    List<ReportRubric> rubricsForReport(long reportId);

    // acknowledgements (D22)
    void acknowledge(long reportId, long parentId);

    /** When this parent acknowledged the report, if at all. */
    Optional<java.time.LocalDateTime> acknowledgedAt(long reportId, long parentId);

    /** Latest acknowledgement across any parent (staff view). */
    Optional<java.time.LocalDateTime> latestAcknowledgement(long reportId);
}
