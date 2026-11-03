package com.school.calendar;

import com.school.model.SchoolCalendarEvent;
import com.school.model.Student;
import com.school.model.TherapySession;
import com.school.model.User;
import com.school.service.StudentService;
import com.school.util.Lookups;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/** Merges therapy sessions, IEP goal targets and school events into calendar events. */
@Service
public class CalendarService {

    private static final String COLOR_THERAPY = "#0dcaf0";
    private static final String COLOR_GOAL = "#fd7e14";
    private static final String COLOR_SCHOOL = "#198754";

    private final CalendarRepository repo;
    private final StudentService students;

    public CalendarService(CalendarRepository repo, StudentService students) {
        this.repo = repo;
        this.students = students;
    }

    public List<CalendarEvent> events(User user, Long studentId, LocalDate from, LocalDate to) {
        List<Long> ids = resolveStudentIds(user, studentId);
        List<CalendarEvent> events = new ArrayList<>();

        for (TherapySession t : repo.therapyInRange(ids, from, to)) {
            events.add(new CalendarEvent(
                    Lookups.label(t.getTherapyType()) + " · " + t.getStudentName(),
                    t.getStartTime().toString(),
                    t.getEndTime() == null ? null : t.getEndTime().toString(),
                    false, COLOR_THERAPY, "THERAPY",
                    "Therapist: " + t.getTherapistName()
                            + (t.getLocation() != null ? " · " + t.getLocation() : "")));
        }
        for (GoalTargetRow g : repo.goalTargetsInRange(ids, from, to)) {
            events.add(new CalendarEvent(
                    "Goal due · " + Lookups.label(g.getDomain()) + " · " + g.getStudentName(),
                    g.getTargetDate().toString(), null, true, COLOR_GOAL, "GOAL",
                    g.getDescription()));
        }
        for (SchoolCalendarEvent s : repo.schoolCalendarInRange(from, to)) {
            events.add(new CalendarEvent(
                    s.getEventName(), s.getEventDate().toString(), null, true,
                    COLOR_SCHOOL, "SCHOOL", s.getDescription()));
        }
        return events;
    }

    private List<Long> resolveStudentIds(User user, Long studentId) {
        List<Long> ids = new ArrayList<>();
        for (Student s : students.visibleRoster(user, null, null, null, null)) {
            ids.add(s.getId());
        }
        if (studentId != null && ids.contains(studentId)) {
            return List.of(studentId);
        }
        return ids;
    }
}
