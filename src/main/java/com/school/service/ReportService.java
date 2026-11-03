package com.school.service;

import com.school.dao.DailyReportDao;
import com.school.model.DailyReport;
import com.school.model.ReportRubric;
import com.school.model.User;
import com.school.util.AccessDeniedException;
import com.school.util.Lookups;
import com.school.util.MyBatis;
import com.school.util.ValidationException;

import java.util.List;

/**
 * Daily progress reports with rubric scoring. Saving a report is a two-step
 * operation: insert the report, then insert one rubric row per criterion.
 * Reports start as DRAFT and must be published before parents can see them.
 */
public class ReportService {

    private final DailyReportDao reportDao;
    private final AccessControl access;

    public ReportService(DailyReportDao reportDao, AccessControl access) {
        this.reportDao = reportDao;
        this.access = access;
    }

    /** Reports for a student. Parents only ever receive PUBLISHED reports. */
    public List<DailyReport> reportsFor(User user, long studentId) {
        access.require(user, studentId);
        boolean publishedOnly = user.isParent();
        return reportDao.reportsForStudent(studentId, publishedOnly);
    }

    public DailyReport getWithRubrics(User user, long reportId) {
        DailyReport r = reportDao.findReport(reportId)
                .orElseThrow(() -> new ValidationException("Report not found."));
        access.require(user, r.getStudentId());
        if (user.isParent() && !r.isPublished()) {
            throw new ValidationException("Report not found.");
        }
        r.setRubrics(reportDao.rubricsForReport(reportId));
        // Acknowledgement status: a parent sees their own; staff see the latest parent ack.
        java.util.Optional<java.time.LocalDateTime> ack = user.isParent()
                ? reportDao.acknowledgedAt(reportId, user.getId())
                : reportDao.latestAcknowledgement(reportId);
        r.setAcknowledged(ack.isPresent());
        r.setAcknowledgedAt(ack.orElse(null));
        return r;
    }

    /**
     * Records a parent's acknowledgement of a published report (idempotent).
     * Routed through {@link #getWithRubrics} so access scope and the
     * published-only rule are enforced before the write.
     */
    public void acknowledge(User parent, long reportId) {
        if (!parent.isParent()) {
            throw new ValidationException("Only parents acknowledge reports.");
        }
        DailyReport r = getWithRubrics(parent, reportId); // enforces access + published-only
        reportDao.acknowledge(r.getId(), parent.getId());
    }

    /**
     * Creates a draft report plus its rubric rows. {@code ratings} is criterion ->
     * rating; {@code comments} is criterion -> note. Only staff with access to the
     * student may author.
     */
    public long createDraft(User author, DailyReport report, java.util.Map<String, String> ratings,
                            java.util.Map<String, String> comments) {
        requireAuthor(author, report.getStudentId());
        if (report.getReportDate() == null) {
            throw new ValidationException("Report date is required.");
        }
        if (isBlank(report.getSummary())) {
            throw new ValidationException("A written summary is required.");
        }
        if (reportDao.dateExists(report.getStudentId(), report.getReportDate(), 0)) {
            throw new ValidationException("A report already exists for this student on "
                    + report.getReportDate() + ".");
        }
        validateRubrics(ratings); // fail before any write, not half-way through
        report.setStatus("DRAFT");
        // Report + its rubric rows must persist together or not at all.
        return MyBatis.tx(() -> {
            long reportId = reportDao.insertReport(report);
            saveRubrics(reportId, ratings, comments);
            return reportId;
        });
    }

    public void updateDraft(User author, DailyReport report, java.util.Map<String, String> ratings,
                            java.util.Map<String, String> comments) {
        DailyReport existing = reportDao.findReport(report.getId())
                .orElseThrow(() -> new ValidationException("Report not found."));
        requireAuthor(author, existing.getStudentId());
        if (existing.isPublished()) {
            throw new ValidationException("Published reports cannot be edited. Unpublish it first.");
        }
        validateRubrics(ratings); // fail before deleting the old rubrics
        // Replace the rubric set atomically so a mid-write failure can't lose them.
        MyBatis.tx(() -> {
            reportDao.updateReport(report);
            reportDao.deleteRubricsForReport(report.getId());
            saveRubrics(report.getId(), ratings, comments);
        });
    }

    public void publish(User author, long reportId) {
        DailyReport r = reportDao.findReport(reportId)
                .orElseThrow(() -> new ValidationException("Report not found."));
        requireAuthor(author, r.getStudentId());
        reportDao.publish(reportId);
    }

    public void unpublish(User author, long reportId) {
        DailyReport r = reportDao.findReport(reportId)
                .orElseThrow(() -> new ValidationException("Report not found."));
        requireAuthor(author, r.getStudentId());
        reportDao.unpublish(reportId);
    }

    /** Rejects unknown ratings up front so no rows are written on bad input. */
    private void validateRubrics(java.util.Map<String, String> ratings) {
        if (ratings == null) {
            return;
        }
        for (String criterion : Lookups.RUBRIC_CRITERIA) {
            String rating = ratings.get(criterion);
            if (rating != null && !rating.isEmpty() && !Lookups.RUBRIC_RATINGS.contains(rating)) {
                throw new ValidationException("Invalid rating for " + criterion + ".");
            }
        }
    }

    private void saveRubrics(long reportId, java.util.Map<String, String> ratings,
                             java.util.Map<String, String> comments) {
        if (ratings == null) {
            return;
        }
        for (String criterion : Lookups.RUBRIC_CRITERIA) {
            String rating = ratings.get(criterion);
            if (rating == null || rating.isEmpty()) {
                continue;
            }
            if (!Lookups.RUBRIC_RATINGS.contains(rating)) {
                throw new ValidationException("Invalid rating for " + criterion + ".");
            }
            ReportRubric r = new ReportRubric();
            r.setReportId(reportId);
            r.setCriterion(criterion);
            r.setRating(rating);
            r.setComment(comments == null ? null : comments.get(criterion));
            reportDao.insertRubric(r);
        }
    }

    /**
     * Authoring guard: only staff (admin/teacher) with access to the student may
     * create, edit, publish or unpublish a report. Parents are read-only here even
     * for their own children.
     */
    private void requireAuthor(User user, long studentId) {
        if (user == null || user.isParent()) {
            throw new AccessDeniedException("Only teachers and admins can author reports.");
        }
        access.require(user, studentId);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
