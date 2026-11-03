package com.school.service;

import com.school.dao.TherapyDao;
import com.school.model.TherapySession;
import com.school.model.User;
import com.school.util.Lookups;
import com.school.util.ValidationException;

import java.util.List;

/** Therapy scheduling with same-student time-conflict detection on save. */
public class TherapyService {

    private final TherapyDao therapyDao;
    private final AccessControl access;

    public TherapyService(TherapyDao therapyDao, AccessControl access) {
        this.therapyDao = therapyDao;
        this.access = access;
    }

    public List<TherapySession> forStudent(User user, long studentId) {
        access.require(user, studentId);
        return therapyDao.forStudent(studentId);
    }

    /**
     * Combined weekly timetable for a student keyed by day of week (Monday first).
     * The schema models therapy sessions only, so the grid is assembled from them;
     * the same structure would merge class sessions in a fuller deployment.
     */
    public java.util.Map<String, List<TherapySession>> weeklyGrid(User user, long studentId) {
        access.require(user, studentId);
        java.util.Map<String, List<TherapySession>> grid = new java.util.LinkedHashMap<>();
        for (java.time.DayOfWeek d : java.time.DayOfWeek.values()) {
            grid.put(d.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH),
                    new java.util.ArrayList<>());
        }
        for (TherapySession s : therapyDao.forStudent(studentId)) {
            if (s.getStartTime() == null) {
                continue;
            }
            String day = s.getStartTime().getDayOfWeek()
                    .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);
            grid.get(day).add(s);
        }
        return grid;
    }

    public TherapySession get(User user, long id) {
        TherapySession s = therapyDao.find(id)
                .orElseThrow(() -> new ValidationException("Session not found."));
        access.require(user, s.getStudentId());
        return s;
    }

    public long schedule(TherapySession s) {
        validate(s);
        checkConflicts(s);
        if (!Lookups.THERAPY_STATUS.contains(s.getStatus())) {
            s.setStatus("SCHEDULED");
        }
        return therapyDao.insert(s);
    }

    public void update(TherapySession s) {
        validate(s);
        checkConflicts(s);
        therapyDao.update(s);
    }

    /**
     * Automatic conflict detection on the three dimensions that matter for a
     * timetable: the student, the teacher/therapist and the room. A cancelled
     * session never blocks. The message names exactly what clashes so the admin
     * can fix it. {@code id == 0} for a new session excludes nothing.
     */
    private void checkConflicts(TherapySession s) {
        long exclude = s.getId();
        if (therapyDao.hasConflict(s.getStudentId(), s.getStartTime(), s.getEndTime(), exclude)) {
            throw new ValidationException("This student already has a session in that time slot.");
        }
        if (s.getTherapistId() > 0
                && therapyDao.hasTherapistConflict(s.getTherapistId(), s.getStartTime(), s.getEndTime(), exclude)) {
            throw new ValidationException("That teacher/therapist is already booked in that time slot.");
        }
        if (s.getLocation() != null && !s.getLocation().isBlank()
                && therapyDao.hasRoomConflict(s.getLocation(), s.getStartTime(), s.getEndTime(), exclude)) {
            throw new ValidationException("Room \"" + s.getLocation() + "\" is already booked in that time slot.");
        }
    }

    /**
     * Flat list of every session for the given students that starts in the
     * Monday-anchored week. The {@code ids} are expected to be a roster the
     * caller already resolved through {@link StudentService#visibleRoster}
     * (same pattern as the calendar feed), so access is already enforced.
     */
    public List<TherapySession> weekSessions(List<Long> ids, java.time.LocalDate weekStart) {
        return therapyDao.forStudentsInRange(ids,
                weekStart.atStartOfDay(), weekStart.plusDays(7).atStartOfDay());
    }

    /** One-click status change used by the teacher "mark completed" action. */
    public void setStatus(User user, long id, String status) {
        TherapySession s = get(user, id);
        if (!Lookups.THERAPY_STATUS.contains(status)) {
            throw new ValidationException("Invalid session status.");
        }
        s.setStatus(status);
        therapyDao.update(s);
    }

    public void delete(long id) {
        therapyDao.delete(id);
    }

    private void validate(TherapySession s) {
        if (!Lookups.THERAPY_TYPES.contains(s.getTherapyType())) {
            throw new ValidationException("Invalid therapy type.");
        }
        if (s.getStartTime() == null || s.getEndTime() == null) {
            throw new ValidationException("Start and end times are required.");
        }
        if (!s.getEndTime().isAfter(s.getStartTime())) {
            throw new ValidationException("End time must be after the start time.");
        }
    }
}
