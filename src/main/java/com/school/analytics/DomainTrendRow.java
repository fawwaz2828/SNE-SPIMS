package com.school.analytics;

import java.time.LocalDate;

/** One aggregated progress point: average goal progress for a domain on a date. */
public class DomainTrendRow {
    private String domain;
    private LocalDate recordedOn;
    private double avgProgress;

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public LocalDate getRecordedOn() { return recordedOn; }
    public void setRecordedOn(LocalDate recordedOn) { this.recordedOn = recordedOn; }

    public double getAvgProgress() { return avgProgress; }
    public void setAvgProgress(double avgProgress) { this.avgProgress = avgProgress; }
}
