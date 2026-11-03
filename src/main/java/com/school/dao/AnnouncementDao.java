package com.school.dao;

import com.school.model.Announcement;

import java.util.List;
import java.util.Optional;

/** Data access for announcements. */
public interface AnnouncementDao {

    long insert(Announcement a);

    void update(Announcement a);

    void setActive(long id, boolean active);

    void delete(long id);

    Optional<Announcement> find(long id);

    /** All announcements (admin management view). */
    List<Announcement> all();

    /**
     * Active, in-window announcements relevant to a role (ALL + role-targeted),
     * newest first. {@code limit <= 0} returns the full list.
     */
    List<Announcement> activeForRole(String role, int limit);
}
