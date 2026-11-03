package com.school.dao;

import com.school.model.TherapySession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Data access for therapy sessions (with overlap detection). */
public interface TherapyDao {

    long insert(TherapySession s);

    void update(TherapySession s);

    void delete(long id);

    Optional<TherapySession> find(long id);

    List<TherapySession> forStudent(long studentId);

    List<TherapySession> forTherapist(long therapistId);

    /**
     * Returns true if the student already has a session overlapping the given
     * window (excluding {@code excludeId}). Used by the Service to reject
     * conflicting bookings.
     */
    boolean hasConflict(long studentId, LocalDateTime start, LocalDateTime end, long excludeId);

    /** True if the teacher/therapist is already booked in the given window. */
    boolean hasTherapistConflict(long therapistId, LocalDateTime start, LocalDateTime end, long excludeId);

    /** True if the room (location) is already booked in the given window. */
    boolean hasRoomConflict(String location, LocalDateTime start, LocalDateTime end, long excludeId);

    /** Every session for the given students that starts within {@code [from, to)}. */
    List<TherapySession> forStudentsInRange(List<Long> ids, LocalDateTime from, LocalDateTime to);
}
