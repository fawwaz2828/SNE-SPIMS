package com.school.web;

import com.school.model.BehaviorIncident;
import com.school.model.User;
import com.school.notify.event.IncidentReportedEvent;
import com.school.security.SecurityUtils;
import com.school.service.IncidentService;
import com.school.service.StudentService;
import com.school.util.AccessDeniedException;
import com.school.util.Lookups;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Behavioural incident logging. Role-scoped lists; staff log incidents. */
@Controller
@RequestMapping("/incidents")
public class IncidentController {

    private final IncidentService incidents;
    private final StudentService students;
    private final ApplicationEventPublisher events;

    public IncidentController(IncidentService incidents, StudentService students,
                              ApplicationEventPublisher events) {
        this.incidents = incidents;
        this.students = students;
        this.events = events;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long studentId,
                       @RequestParam(required = false) String severity,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                       Model model) {
        User user = SecurityUtils.currentUser();
        model.addAttribute("roster", students.visibleRoster(user, null, null, null, null));
        model.addAttribute("severities", Lookups.SEVERITIES);
        model.addAttribute("studentId", studentId);
        model.addAttribute("severity", severity);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("canLog", isStaff(user));

        // Parents must pick a child before any incidents are shown.
        if (user.isParent() && studentId == null) {
            model.addAttribute("incidents", java.util.List.of());
            model.addAttribute("needsStudent", true);
        } else {
            model.addAttribute("incidents",
                    incidents.view(user, studentId, blankToNull(severity), from, to));
            model.addAttribute("needsStudent", false);
        }
        return "incidents/list";
    }

    @GetMapping("/new")
    public String newForm(@RequestParam(required = false) Long studentId, Model model) {
        User user = SecurityUtils.currentUser();
        requireStaff(user);
        model.addAttribute("roster", students.visibleRoster(user, null, null, null, null));
        model.addAttribute("severities", Lookups.SEVERITIES);
        model.addAttribute("selectedStudentId", studentId);
        return "incidents/form";
    }

    @PostMapping
    public String log(@RequestParam long studentId,
                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime incidentDate,
                      @RequestParam(required = false) String location,
                      @RequestParam String description,
                      @RequestParam String severity,
                      @RequestParam(required = false) String actionTaken,
                      @RequestParam(defaultValue = "false") boolean parentVisible,
                      RedirectAttributes ra) {
        User user = SecurityUtils.currentUser();
        requireStaff(user);
        BehaviorIncident i = new BehaviorIncident();
        i.setStudentId(studentId);
        i.setIncidentDate(incidentDate);
        i.setLocation(blankToNull(location));
        i.setDescription(description);
        i.setSeverity(severity);
        i.setActionTaken(blankToNull(actionTaken));
        i.setParentVisible(parentVisible);
        long id = incidents.log(user, i);
        // Notify parents when the incident is visible to them (listener checks parentVisible).
        BehaviorIncident saved = incidents.get(user, id);
        events.publishEvent(new IncidentReportedEvent(saved.getStudentId(), saved.getStudentName(),
                saved.getId(), saved.getSeverity(), saved.isParentVisible()));
        ra.addFlashAttribute("flashSuccess", "Incident logged.");
        return "redirect:/incidents/" + id;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable long id, Model model) {
        User user = SecurityUtils.currentUser();
        model.addAttribute("incident", incidents.get(user, id));
        return "incidents/detail";
    }

    private boolean isStaff(User u) {
        return u != null && (u.isAdmin() || u.isTeacher());
    }

    private void requireStaff(User u) {
        if (!isStaff(u)) {
            throw new AccessDeniedException("Only teachers and admins can log incidents.");
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
