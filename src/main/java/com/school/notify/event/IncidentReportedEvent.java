package com.school.notify.event;

/**
 * Published when a behaviour incident is recorded. The listener only notifies
 * parents when {@code parentVisible} is true.
 */
public class IncidentReportedEvent {
    private final long studentId;
    private final String studentName;
    private final long incidentId;
    private final String severity;
    private final boolean parentVisible;

    public IncidentReportedEvent(long studentId, String studentName, long incidentId,
                                 String severity, boolean parentVisible) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.incidentId = incidentId;
        this.severity = severity;
        this.parentVisible = parentVisible;
    }

    public long getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public long getIncidentId() { return incidentId; }
    public String getSeverity() { return severity; }
    public boolean isParentVisible() { return parentVisible; }
}
