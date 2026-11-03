package com.school.web;

import com.school.model.Student;
import com.school.model.TherapySession;
import com.school.model.User;
import com.school.notify.NotificationService;
import com.school.security.SecurityUtils;
import com.school.service.StudentService;
import com.school.service.TherapyService;
import com.school.service.UserService;
import com.school.util.AccessDeniedException;
import com.school.util.Lookups;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Therapy session scheduling per student. Staff schedule; parents read-only. */
@Controller
@RequestMapping("/students/{studentId}/therapy")
public class TherapyController {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("EEE dd MMM, HH:mm");

    private final TherapyService therapy;
    private final StudentService students;
    private final UserService users;
    private final NotificationService notifications;

    public TherapyController(TherapyService therapy, StudentService students,
                            UserService users, NotificationService notifications) {
        this.therapy = therapy;
        this.students = students;
        this.users = users;
        this.notifications = notifications;
    }

    @GetMapping
    public String list(@PathVariable long studentId, Model model) {
        User user = SecurityUtils.currentUser();
        model.addAttribute("student", students.get(user, studentId));
        model.addAttribute("sessions", therapy.forStudent(user, studentId));
        model.addAttribute("canEdit", isStaff(user));
        return "therapy/list";
    }

    @GetMapping("/new")
    public String newForm(@PathVariable long studentId, Model model) {
        User user = SecurityUtils.currentUser();
        requireStaff(user);
        model.addAttribute("student", students.get(user, studentId));
        TherapySession blank = new TherapySession();
        blank.setTherapistId(user.getId());   // sensible default; admin can reassign
        model.addAttribute("session", blank);
        addLookups(model);
        return "therapy/form";
    }

    @GetMapping("/{sessionId}/edit")
    public String editForm(@PathVariable long studentId, @PathVariable long sessionId, Model model) {
        User user = SecurityUtils.currentUser();
        requireStaff(user);
        model.addAttribute("student", students.get(user, studentId));
        model.addAttribute("session", therapy.get(user, sessionId));
        addLookups(model);
        return "therapy/form";
    }

    @PostMapping
    public String save(@PathVariable long studentId,
                       @RequestParam(defaultValue = "0") long id,
                       @RequestParam(defaultValue = "0") long therapistId,
                       @RequestParam String therapyType,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
                       @RequestParam(required = false) String location,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String notes,
                       RedirectAttributes ra) {
        User user = SecurityUtils.currentUser();
        requireStaff(user);
        Student student = students.get(user, studentId); // access check
        TherapySession s = (id == 0) ? new TherapySession() : therapy.get(user, id);
        s.setStudentId(studentId);
        // Admin explicitly assigns the teacher/therapist; fall back to the
        // current staff member if none was chosen (e.g. a teacher self-booking).
        s.setTherapistId(therapistId > 0 ? therapistId : user.getId());
        s.setTherapyType(therapyType);
        s.setStartTime(startTime);
        s.setEndTime(endTime);
        s.setLocation(blankToNull(location));
        s.setStatus(status);
        s.setNotes(blankToNull(notes));
        if (id == 0) {
            therapy.schedule(s);
            notifySession(s, student.getFullName(), "scheduled");
            ra.addFlashAttribute("flashSuccess", "Therapy session scheduled. Teacher and parents notified.");
        } else {
            therapy.update(s);
            notifySession(s, student.getFullName(), "updated");
            ra.addFlashAttribute("flashSuccess", "Therapy session updated. Teacher and parents notified.");
        }
        return "redirect:/students/" + studentId + "/therapy";
    }

    @PostMapping("/{sessionId}/complete")
    public String complete(@PathVariable long studentId, @PathVariable long sessionId, RedirectAttributes ra) {
        User user = SecurityUtils.currentUser();
        requireStaff(user);
        therapy.setStatus(user, sessionId, "COMPLETED");
        ra.addFlashAttribute("flashSuccess", "Session marked completed.");
        return "redirect:/students/" + studentId + "/therapy";
    }

    @PostMapping("/{sessionId}/delete")
    public String delete(@PathVariable long studentId, @PathVariable long sessionId, RedirectAttributes ra) {
        User user = SecurityUtils.currentUser();
        requireStaff(user);
        TherapySession s = therapy.get(user, sessionId); // access check + capture for notice
        Student student = students.get(user, studentId);
        therapy.delete(sessionId);
        notifySession(s, student.getFullName(), "cancelled");
        ra.addFlashAttribute("flashSuccess", "Session cancelled. Teacher and parents notified.");
        return "redirect:/students/" + studentId + "/therapy";
    }

    /**
     * Fires the schedule-change notification to the assigned teacher and to the
     * student's parents — the single place the redesign routes create/update/
     * cancel events through.
     */
    private void notifySession(TherapySession s, String studentName, String verb) {
        String when = s.getStartTime() == null ? "soon" : s.getStartTime().format(WHEN);
        String type = Lookups.label(s.getTherapyType());
        String link = "/students/" + s.getStudentId() + "/therapy";
        // "THERAPY_REMINDER" is the schedule-related notification type the schema
        // allows (notifications.type is a fixed ENUM); it drives the bell badge.
        notifications.notifyUser(s.getTherapistId(), "THERAPY_REMINDER",
                "Session " + verb,
                type + " session with " + studentName + " " + verb + " for " + when + ".",
                link, s.getStudentId());
        notifications.notifyParentsOfStudent(s.getStudentId(), "THERAPY_REMINDER",
                "Session " + verb,
                type + " therapy for " + studentName + " " + verb + " for " + when + ".",
                "/calendar");
    }

    private void addLookups(Model model) {
        model.addAttribute("therapyTypes", Lookups.THERAPY_TYPES);
        model.addAttribute("statuses", Lookups.THERAPY_STATUS);
        model.addAttribute("therapists", users.activeByRole("TEACHER"));
    }

    private boolean isStaff(User u) {
        return u != null && (u.isAdmin() || u.isTeacher());
    }

    private void requireStaff(User u) {
        if (!isStaff(u)) {
            throw new AccessDeniedException("Only teachers and admins can manage therapy sessions.");
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
