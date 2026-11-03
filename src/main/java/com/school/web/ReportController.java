package com.school.web;

import com.school.model.DailyReport;
import com.school.model.ReportRubric;
import com.school.model.User;
import com.school.notify.NotificationService;
import com.school.notify.event.DailyReportPublishedEvent;
import com.school.security.SecurityUtils;
import com.school.service.ReportService;
import com.school.service.StudentService;
import com.school.util.AccessDeniedException;
import com.school.util.Lookups;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Daily progress reports with rubric scoring. Staff author/publish; parents read published. */
@Controller
public class ReportController {

    private final ReportService reports;
    private final StudentService students;
    private final ApplicationEventPublisher events;
    private final NotificationService notifications;

    public ReportController(ReportService reports, StudentService students,
                            ApplicationEventPublisher events, NotificationService notifications) {
        this.reports = reports;
        this.students = students;
        this.events = events;
        this.notifications = notifications;
    }

    @GetMapping("/students/{studentId}/reports")
    public String list(@PathVariable long studentId, Model model) {
        User user = SecurityUtils.currentUser();
        model.addAttribute("student", students.get(user, studentId));
        model.addAttribute("reports", reports.reportsFor(user, studentId));
        model.addAttribute("canEdit", isStaff(user));
        return "reports/list";
    }

    @GetMapping("/students/{studentId}/reports/new")
    public String newForm(@PathVariable long studentId, Model model) {
        User user = SecurityUtils.currentUser();
        requireStaff(user);
        model.addAttribute("student", students.get(user, studentId));
        model.addAttribute("report", new DailyReport());
        model.addAttribute("criteria", Lookups.RUBRIC_CRITERIA);
        model.addAttribute("ratings", Lookups.RUBRIC_RATINGS);
        model.addAttribute("moods", Lookups.MOODS);
        model.addAttribute("ratingByCriterion", new HashMap<String, String>());
        model.addAttribute("commentByCriterion", new HashMap<String, String>());
        return "reports/form";
    }

    @PostMapping("/students/{studentId}/reports")
    public String create(@PathVariable long studentId,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate,
                         @RequestParam String summary,
                         @RequestParam(required = false) String mood,
                         @RequestParam(required = false) List<String> criterion,
                         @RequestParam(required = false) List<String> rating,
                         @RequestParam(required = false) List<String> comment,
                         RedirectAttributes ra) {
        User user = SecurityUtils.currentUser();
        requireStaff(user);
        students.get(user, studentId); // access check
        DailyReport r = new DailyReport();
        r.setStudentId(studentId);
        r.setTeacherId(user.getId());
        r.setReportDate(reportDate);
        r.setSummary(summary);
        r.setMood(mood);
        long id = reports.createDraft(user, r, toMap(criterion, rating), toMap(criterion, comment));
        ra.addFlashAttribute("flashSuccess", "Report saved as draft.");
        return "redirect:/reports/" + id;
    }

    @GetMapping("/reports/{id}")
    public String detail(@PathVariable long id, Model model) {
        User user = SecurityUtils.currentUser();
        DailyReport report = reports.getWithRubrics(user, id);
        model.addAttribute("report", report);
        model.addAttribute("canEdit", isStaff(user));
        return "reports/detail";
    }

    @GetMapping("/reports/{id}/edit")
    public String editForm(@PathVariable long id, Model model) {
        User user = SecurityUtils.currentUser();
        requireStaff(user);
        DailyReport report = reports.getWithRubrics(user, id);
        Map<String, String> ratingByCriterion = new HashMap<>();
        Map<String, String> commentByCriterion = new HashMap<>();
        for (ReportRubric rb : report.getRubrics()) {
            ratingByCriterion.put(rb.getCriterion(), rb.getRating());
            commentByCriterion.put(rb.getCriterion(), rb.getComment());
        }
        model.addAttribute("student", students.get(user, report.getStudentId()));
        model.addAttribute("report", report);
        model.addAttribute("criteria", Lookups.RUBRIC_CRITERIA);
        model.addAttribute("ratings", Lookups.RUBRIC_RATINGS);
        model.addAttribute("moods", Lookups.MOODS);
        model.addAttribute("ratingByCriterion", ratingByCriterion);
        model.addAttribute("commentByCriterion", commentByCriterion);
        return "reports/form";
    }

    @PostMapping("/reports/{id}")
    public String update(@PathVariable long id,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate,
                         @RequestParam String summary,
                         @RequestParam(required = false) String mood,
                         @RequestParam(required = false) List<String> criterion,
                         @RequestParam(required = false) List<String> rating,
                         @RequestParam(required = false) List<String> comment,
                         RedirectAttributes ra) {
        User user = SecurityUtils.currentUser();
        requireStaff(user);
        DailyReport existing = reports.getWithRubrics(user, id); // access check
        existing.setReportDate(reportDate);
        existing.setSummary(summary);
        existing.setMood(mood);
        reports.updateDraft(user, existing, toMap(criterion, rating), toMap(criterion, comment));
        ra.addFlashAttribute("flashSuccess", "Report updated.");
        return "redirect:/reports/" + id;
    }

    @PostMapping("/reports/{id}/publish")
    public String publish(@PathVariable long id, RedirectAttributes ra) {
        User user = SecurityUtils.currentUser();
        requireStaff(user);
        DailyReport r = reports.getWithRubrics(user, id); // access check
        reports.publish(user, id);
        // Notify the student's parents (async listener -> NotificationService).
        events.publishEvent(new DailyReportPublishedEvent(r.getStudentId(), r.getStudentName(), r.getId()));
        ra.addFlashAttribute("flashSuccess", "Report published — parents can now see it.");
        return "redirect:/reports/" + id;
    }

    @PostMapping("/reports/{id}/unpublish")
    public String unpublish(@PathVariable long id, RedirectAttributes ra) {
        User user = SecurityUtils.currentUser();
        requireStaff(user);
        reports.getWithRubrics(user, id); // access check
        reports.unpublish(user, id);
        ra.addFlashAttribute("flashSuccess", "Report unpublished.");
        return "redirect:/reports/" + id;
    }

    /** Process 4.5 — parent acknowledges a published report (feedback into Daily Reporting). */
    @PostMapping("/reports/{id}/acknowledge")
    public String acknowledge(@PathVariable long id, RedirectAttributes ra) {
        User user = SecurityUtils.currentUser();
        reports.acknowledge(user, id); // service enforces parent + access + published-only
        // Let the authoring teacher know the parent has read it.
        DailyReport r = reports.getWithRubrics(user, id);
        notifications.notifyUser(r.getTeacherId(), "GENERAL", "Report acknowledged",
                user.getFullName() + " acknowledged the report for " + r.getStudentName() + ".",
                "/reports/" + id, r.getStudentId());
        ra.addFlashAttribute("flashSuccess", "Thank you — your acknowledgement has been recorded.");
        return "redirect:/reports/" + id;
    }

    /** Builds a criterion->value map from two parallel lists, skipping blank values. */
    private static Map<String, String> toMap(List<String> keys, List<String> values) {
        Map<String, String> map = new HashMap<>();
        if (keys == null || values == null) {
            return map;
        }
        for (int i = 0; i < keys.size() && i < values.size(); i++) {
            String v = values.get(i);
            if (v != null && !v.isEmpty()) {
                map.put(keys.get(i), v);
            }
        }
        return map;
    }

    private boolean isStaff(User u) {
        return u != null && (u.isAdmin() || u.isTeacher());
    }

    private void requireStaff(User u) {
        if (!isStaff(u)) {
            throw new AccessDeniedException("Only teachers and admins can author reports.");
        }
    }
}
