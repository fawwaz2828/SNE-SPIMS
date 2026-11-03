package com.school.notify.event;

/**
 * Published when a daily report is published. Consumed asynchronously to notify
 * the student's parents. (Publish from the report flow once that module is ported:
 * {@code publisher.publishEvent(new DailyReportPublishedEvent(...))}.)
 */
public class DailyReportPublishedEvent {
    private final long studentId;
    private final String studentName;
    private final long reportId;

    public DailyReportPublishedEvent(long studentId, String studentName, long reportId) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.reportId = reportId;
    }

    public long getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public long getReportId() { return reportId; }
}
