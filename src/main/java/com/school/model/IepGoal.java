package com.school.model;

import java.time.LocalDate;

/** A measurable goal within an IEP, categorised by developmental domain. */
public class IepGoal {

    private long id;
    private long iepId;
    private String domain;       // see Lookups.DOMAINS
    private String description;
    private LocalDate targetDate;
    private String status;       // NOT_STARTED | IN_PROGRESS | ACHIEVED | DISCONTINUED
    private int progressPct;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getIepId() { return iepId; }
    public void setIepId(long iepId) { this.iepId = iepId; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getProgressPct() { return progressPct; }
    public void setProgressPct(int progressPct) { this.progressPct = progressPct; }
}
