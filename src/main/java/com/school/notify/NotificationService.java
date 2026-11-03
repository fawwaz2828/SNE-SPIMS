package com.school.notify;

import com.school.dao.AssignmentDao;
import com.school.model.Assignment;
import com.school.model.Notification;
import com.school.model.TherapySession;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

/** Creates, queries and dispatches notifications. */
@Service
public class NotificationService {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("EEE dd MMM, HH:mm");

    private final NotificationRepository repo;
    private final AssignmentDao assignments;

    public NotificationService(NotificationRepository repo, AssignmentDao assignments) {
        this.repo = repo;
        this.assignments = assignments;
    }

    // --- queries used by the controller / bell ---

    public long unreadCount(long userId) {
        return repo.unreadCount(userId);
    }

    public List<Notification> inbox(long userId, boolean includeArchived) {
        return repo.listForUser(userId, includeArchived);
    }

    public void markRead(long id, long userId) {
        repo.markRead(id, userId);
    }

    public void markAllRead(long userId) {
        repo.markAllRead(userId);
    }

    public void archive(long id, long userId) {
        repo.archive(id, userId);
    }

    // --- creation ---

    public void notifyUser(long userId, String type, String title, String message, String link, Long studentId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setLink(link);
        n.setStudentId(studentId);
        repo.insert(n);
    }

    /** Notify every parent/guardian linked to a student (all or none). */
    public void notifyParentsOfStudent(long studentId, String type, String title, String message, String link) {
        com.school.util.MyBatis.tx(() -> {
            for (Assignment link2 : assignments.parentLinks(studentId)) {
                notifyUser(link2.getUserId(), type, title, message, link, studentId);
            }
        });
    }

    // --- event-driven handlers (called by listeners) ---

    public void onDailyReportPublished(long studentId, String studentName, long reportId) {
        notifyParentsOfStudent(studentId, "DAILY_REPORT",
                "New daily report",
                "A new daily report for " + studentName + " has been published.",
                "/reports/" + reportId);
    }

    public void onParentVisibleIncident(long studentId, String studentName, long incidentId, String severity) {
        notifyParentsOfStudent(studentId, "INCIDENT",
                "Behaviour incident logged",
                "A " + severity + " incident was recorded for " + studentName + ".",
                "/incidents?studentId=" + studentId);
    }

    /** Called by the 07:00 cron: remind therapist + parents of sessions within 2 days. */
    public int sendTherapyReminders() {
        int sent = 0;
        for (TherapySession s : repo.upcomingTherapy()) {
            String when = s.getStartTime().format(WHEN);
            String title = "Upcoming therapy session";
            String msgTherapist = s.getTherapyType() + " session with " + s.getStudentName() + " on " + when + ".";
            notifyUser(s.getTherapistId(), "THERAPY_REMINDER", title, msgTherapist,
                    "/calendar", s.getStudentId());
            sent++;

            String msgParent = s.getTherapyType() + " therapy for " + s.getStudentName()
                    + " is scheduled on " + when + ".";
            notifyParentsOfStudent(s.getStudentId(), "THERAPY_REMINDER", title, msgParent, "/calendar");
        }
        return sent;
    }
}
