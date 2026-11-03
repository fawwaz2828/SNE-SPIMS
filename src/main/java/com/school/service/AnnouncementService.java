package com.school.service;

import com.school.dao.AnnouncementDao;
import com.school.model.Announcement;
import com.school.model.User;
import com.school.util.Lookups;
import com.school.util.ValidationException;

import java.util.List;

/** Announcement management (admin) and role-filtered retrieval (all users). */
public class AnnouncementService {

    /** Number of items shown in the reusable dashboard widget. */
    public static final int WIDGET_LIMIT = 3;

    private final AnnouncementDao announcementDao;

    public AnnouncementService(AnnouncementDao announcementDao) {
        this.announcementDao = announcementDao;
    }

    public List<Announcement> all() {
        return announcementDao.all();
    }

    /** The three most recent announcements relevant to the user's role. */
    public List<Announcement> widgetFor(User user) {
        return announcementDao.activeForRole(user.getRole(), WIDGET_LIMIT);
    }

    /** Full relevant list for the browse page. */
    public List<Announcement> listFor(User user) {
        return announcementDao.activeForRole(user.getRole(), 0);
    }

    public Announcement get(long id) {
        return announcementDao.find(id)
                .orElseThrow(() -> new ValidationException("Announcement not found."));
    }

    public long create(Announcement a) {
        validate(a);
        return announcementDao.insert(a);
    }

    public void update(Announcement a) {
        validate(a);
        announcementDao.update(a);
    }

    public void setActive(long id, boolean active) {
        announcementDao.setActive(id, active);
    }

    public void delete(long id) {
        announcementDao.delete(id);
    }

    private void validate(Announcement a) {
        if (a.getTitle() == null || a.getTitle().trim().isEmpty()) {
            throw new ValidationException("Title is required.");
        }
        if (a.getBody() == null || a.getBody().trim().isEmpty()) {
            throw new ValidationException("Body is required.");
        }
        if (!Lookups.ANNOUNCEMENT_TARGETS.contains(a.getTargetRole())) {
            throw new ValidationException("Invalid target audience.");
        }
    }
}
