package com.school.model;

/** A single criterion rating attached to a daily report. */
public class ReportRubric {

    private long id;
    private long reportId;
    private String criterion;
    private String rating;       // EMERGING | DEVELOPING | PROFICIENT | MASTERED
    private String comment;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getReportId() { return reportId; }
    public void setReportId(long reportId) { this.reportId = reportId; }

    public String getCriterion() { return criterion; }
    public void setCriterion(String criterion) { this.criterion = criterion; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
