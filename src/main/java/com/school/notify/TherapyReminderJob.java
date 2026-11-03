package com.school.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Sends therapy reminders every morning at 07:00 (server time zone). */
@Component
public class TherapyReminderJob {

    private static final Logger log = LoggerFactory.getLogger(TherapyReminderJob.class);

    private final NotificationService notifications;

    public TherapyReminderJob(NotificationService notifications) {
        this.notifications = notifications;
    }

    @Scheduled(cron = "0 0 7 * * *")
    public void run() {
        int sent = notifications.sendTherapyReminders();
        log.info("Therapy reminder job sent {} therapist notifications", sent);
    }
}
