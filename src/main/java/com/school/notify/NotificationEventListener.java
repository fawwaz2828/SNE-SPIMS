package com.school.notify;

import com.school.notify.event.DailyReportPublishedEvent;
import com.school.notify.event.IncidentReportedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/** Turns domain events into parent notifications, off the request thread. */
@Component
public class NotificationEventListener {

    private final NotificationService notifications;

    public NotificationEventListener(NotificationService notifications) {
        this.notifications = notifications;
    }

    @Async
    @EventListener
    public void onDailyReportPublished(DailyReportPublishedEvent e) {
        notifications.onDailyReportPublished(e.getStudentId(), e.getStudentName(), e.getReportId());
    }

    @Async
    @EventListener
    public void onIncidentReported(IncidentReportedEvent e) {
        if (e.isParentVisible()) {
            notifications.onParentVisibleIncident(
                    e.getStudentId(), e.getStudentName(), e.getIncidentId(), e.getSeverity());
        }
    }
}
