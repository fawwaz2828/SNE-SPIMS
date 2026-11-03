package com.school.web;

import com.school.analytics.AnalyticsService;
import com.school.analytics.DashboardData;
import com.school.calendar.CalendarEvent;
import com.school.calendar.CalendarService;
import com.school.model.BehaviorIncident;
import com.school.model.DailyReport;
import com.school.model.IepGoal;
import com.school.model.IepProgram;
import com.school.model.Student;
import com.school.model.TherapySession;
import com.school.model.User;
import com.school.security.SecurityUtils;
import com.school.service.AnnouncementService;
import com.school.service.IepService;
import com.school.service.IncidentService;
import com.school.service.MessageService;
import com.school.service.ReportService;
import com.school.service.StudentService;
import com.school.service.TherapyService;
import com.school.service.UserService;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Role-aware dashboard. Parents get the warm, child-centred design (DESIGN.md). */
@Controller
public class DashboardController {

    /** Look-back / look-ahead windows. Exposed to the view so the copy never
        hard-codes a number that drifts from the query. */
    private static final int INCIDENT_WINDOW_DAYS = 28;
    private static final int REPORT_DUE_DAYS = 7;
    private static final int UPCOMING_WINDOW_DAYS = 14;

    private final AnnouncementService announcements;
    private final AnalyticsService analytics;
    private final StudentService students;
    private final IepService iep;
    private final TherapyService therapy;
    private final ReportService reports;
    private final CalendarService calendar;
    private final UserService users;
    private final MessageService messages;
    private final IncidentService incidents;

    public DashboardController(AnnouncementService announcements,
                               AnalyticsService analytics,
                               StudentService students,
                               IepService iep,
                               TherapyService therapy,
                               ReportService reports,
                               CalendarService calendar,
                               UserService users,
                               MessageService messages,
                               IncidentService incidents) {
        this.announcements = announcements;
        this.analytics = analytics;
        this.students = students;
        this.iep = iep;
        this.therapy = therapy;
        this.reports = reports;
        this.calendar = calendar;
        this.users = users;
        this.messages = messages;
        this.incidents = incidents;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) Long studentId,
                            Model model) {
        User user = SecurityUtils.currentUser();

        if (user != null && user.isParent()) {
            return parentDashboard(user, model, studentId);
        }

        // Period/student filters were removed from the UI; analytics use the defaults.
        List<Student> visible = students.visibleRoster(user, null, null, null, null);
        DashboardData data = analytics.build(user, visible, null, null);
        model.addAttribute("announcements", announcements.widgetFor(user));
        model.addAttribute("data", data);
        model.addAttribute("totalStudents", visible.size());
        // Card-based summary (no charts): disability counts (admin "Students by disability").
        model.addAttribute("disabilityRows", disabilityRows(data));
        // Real, role-scoped operational widgets (sessions, incidents, follow-ups).
        addStaffWidgets(user, visible, model);
        return "dashboard";
    }

    // ---- Teacher / admin operational widgets (all from live data) ------------

    /**
     * Adds the working widgets a teacher or admin actually needs day-to-day, all
     * derived from real records (never fabricated):
     *   • this week's therapy sessions for the visible students,
     *   • recent behaviour incidents,
     *   • (teacher) caseload students with no report in the last 7 days + unread messages,
     *   • (admin) staff head-counts + upcoming school events.
     */
    private void addStaffWidgets(User user, List<Student> visible, Model model) {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.minusDays((today.getDayOfWeek().getValue() + 6) % 7);
        List<Long> ids = new ArrayList<>();
        for (Student s : visible) {
            ids.add(s.getId());
        }

        model.addAttribute("weekSessions", weekSessionRows(user, ids, monday));
        model.addAttribute("recentIncidents",
                recentIncidentRows(user, today.minusDays(INCIDENT_WINDOW_DAYS), today));
        model.addAttribute("incidentDays", INCIDENT_WINDOW_DAYS);

        // Sidebar calendar: the current week (role-scoped events) + month label.
        model.addAttribute("monthYear",
                today.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)));
        model.addAttribute("weekDays", weekStrip(user, null, today));

        if (user.isAdmin()) {
            model.addAttribute("teacherCount", users.activeByRole("TEACHER").size());
            model.addAttribute("parentCount", users.activeByRole("PARENT").size());
            model.addAttribute("sessionsThisWeek", therapy.weekSessions(ids, monday).size());
            model.addAttribute("upcomingEvents",
                    upcomingEventRows(user, today, today.plusDays(UPCOMING_WINDOW_DAYS)));
            model.addAttribute("upcomingDays", UPCOMING_WINDOW_DAYS);
        } else {
            model.addAttribute("unreadMessages", messages.unreadCount(user));
            model.addAttribute("reportsDue", reportsDueRows(user, visible, today));
            model.addAttribute("reportDays", REPORT_DUE_DAYS);
        }
    }

    /** This week's therapy sessions for the given students (max 8, time-ordered). */
    private List<Map<String, Object>> weekSessionRows(User user, List<Long> ids, LocalDate monday) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (ids.isEmpty()) {
            return out;
        }
        List<TherapySession> sessions = new ArrayList<>(therapy.weekSessions(ids, monday));
        sessions.sort((a, b) -> a.getStartTime().compareTo(b.getStartTime()));
        for (TherapySession s : sessions) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", com.school.util.Lookups.label(s.getTherapyType()));
            m.put("student", s.getStudentName());
            m.put("when", s.getStartTime() == null ? "" : s.getStartTime()
                    .format(DateTimeFormatter.ofPattern("EEE · HH:mm", Locale.ENGLISH)));
            m.put("location", s.getLocation());
            m.put("status", s.getStatus());
            out.add(m);
            if (out.size() == 8) {
                break;
            }
        }
        return out;
    }

    /** Most recent behaviour incidents the user may see within the window (max 5). */
    private List<Map<String, Object>> recentIncidentRows(User user, LocalDate from, LocalDate to) {
        List<Map<String, Object>> out = new ArrayList<>();
        List<BehaviorIncident> list = incidents.view(user, null, null, from, to);
        list.sort((a, b) -> b.getIncidentDate().compareTo(a.getIncidentDate()));
        for (BehaviorIncident i : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("student", i.getStudentName());
            m.put("severity", i.getSeverity());
            m.put("desc", i.getDescription());
            m.put("when", i.getIncidentDate() == null ? "" : i.getIncidentDate()
                    .format(DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)));
            out.add(m);
            if (out.size() == 5) {
                break;
            }
        }
        return out;
    }

    /** Caseload students with no PUBLISHED report in the last 7 days — a follow-up list. */
    private List<Map<String, Object>> reportsDueRows(User user, List<Student> visible, LocalDate today) {
        List<Map<String, Object>> out = new ArrayList<>();
        LocalDate cutoff = today.minusDays(REPORT_DUE_DAYS);
        for (Student s : visible) {
            boolean recent = false;
            for (DailyReport r : reports.reportsFor(user, s.getId())) {
                if ("PUBLISHED".equals(r.getStatus()) && r.getReportDate() != null
                        && !r.getReportDate().isBefore(cutoff)) {
                    recent = true;
                    break;
                }
            }
            if (!recent) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", s.getId());
                m.put("name", s.getFullName());
                out.add(m);
            }
        }
        return out;
    }

    /** Upcoming school events and goal-due dates in the window (max 6). */
    private List<Map<String, Object>> upcomingEventRows(User user, LocalDate from, LocalDate to) {
        List<Map<String, Object>> out = new ArrayList<>();
        List<CalendarEvent> events = new ArrayList<>(calendar.events(user, null, from, to));
        events.sort((a, b) -> a.getStart().compareTo(b.getStart()));
        for (CalendarEvent e : events) {
            // Therapy sessions are shown in their own widget; here we surface
            // school events and goal target dates only.
            if (!"SCHOOL".equals(e.getType()) && !"GOAL".equals(e.getType())) {
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("title", e.getTitle());
            m.put("type", e.getType());
            m.put("color", e.getColor());
            m.put("when", e.getStart() != null && e.getStart().length() >= 10
                    ? e.getStart().substring(0, 10) : e.getStart());
            out.add(m);
            if (out.size() == 6) {
                break;
            }
        }
        return out;
    }

    // ---- Parent dashboard (DESIGN.md) ----------------------------------------

    private String parentDashboard(User user, Model model, Long selectedChildId) {
        LocalDate today = LocalDate.now();
        model.addAttribute("announcements", announcements.widgetFor(user));
        model.addAttribute("parentInitials", initials(user.getFullName()));
        model.addAttribute("monthName", today.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        model.addAttribute("monthYear",
                today.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)));

        List<Student> children = students.visibleRoster(user, null, null, null, null);
        if (children.isEmpty()) {
            model.addAttribute("child", null);
            model.addAttribute("weekDays", weekStrip(user, null, today));
            return "parent-dashboard";
        }
        // A parent may have more than one child (siblings). Show the one selected
        // via ?studentId, otherwise the first; expose the list for the switcher.
        Student child = children.get(0);
        if (selectedChildId != null) {
            for (Student c : children) {
                if (c.getId() == selectedChildId) {
                    child = c;
                    break;
                }
            }
        }
        model.addAttribute("children", children);
        model.addAttribute("selectedChildId", child.getId());
        model.addAttribute("child", child);
        model.addAttribute("childFirstName", firstName(child.getFullName()));
        model.addAttribute("childAge", child.getDateOfBirth() == null ? null
                : Period.between(child.getDateOfBirth(), today).getYears());

        // Latest published report (hero "View report").
        List<DailyReport> published = reports.reportsFor(user, child.getId());
        model.addAttribute("latestReport", published.isEmpty() ? null : published.get(0));

        // Progress per development area — only domains the child actually has goals in.
        model.addAttribute("domainCards", domainCards(user, child.getId()));

        // Upcoming therapy sessions (next two).
        model.addAttribute("sessions", upcomingSessions(user, child.getId()));

        // Interactive week strip: each day carries the child's real activities
        // (therapy sessions + school events) so days with something on are marked
        // and clicking a day reveals what's happening.
        model.addAttribute("weekDays", weekStrip(user, child.getId(), today));
        return "parent-dashboard";
    }

    /**
     * One progress card per development area the child actually has goals in
     * (active programs). Each card carries the average progress % plus its
     * colour/icon, so empty domains never show and Motor (etc.) appears.
     */
    private List<Map<String, Object>> domainCards(User user, long childId) {
        Map<String, int[]> agg = new LinkedHashMap<>();
        for (IepProgram p : iep.programsForStudent(user, childId)) {
            if (!"ACTIVE".equals(p.getStatus())) {
                continue;
            }
            for (IepGoal g : iep.getProgram(user, p.getId()).getGoals()) {
                int[] sc = agg.computeIfAbsent(g.getDomain(), k -> new int[2]);
                sc[0] += g.getProgressPct();
                sc[1]++;
            }
        }
        List<Map<String, Object>> cards = new ArrayList<>();
        for (Map.Entry<String, int[]> e : agg.entrySet()) {
            int[] sc = e.getValue();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("label", com.school.util.Lookups.DOMAINS.getOrDefault(e.getKey(), e.getKey()));
            m.put("pct", sc[1] == 0 ? 0 : Math.round(sc[0] * 1f / sc[1]));
            String[] meta = domainMeta(e.getKey());
            m.put("color", meta[0]);
            m.put("bg", meta[1]);
            m.put("icon", meta[2]);
            cards.add(m);
        }
        return cards;
    }

    /** {color, background, bootstrap-icon} for a development domain. */
    private static String[] domainMeta(String domain) {
        switch (domain) {
            case "ACADEMIC":         return new String[]{"var(--d-academic)", "var(--c-blue-bg)",   "bi-book"};
            case "COMMUNICATION":    return new String[]{"var(--d-comm)",     "var(--c-green-bg)",  "bi-chat"};
            case "SOCIAL_EMOTIONAL": return new String[]{"var(--d-social)",   "var(--c-amber-bg)",  "bi-heart"};
            case "SELF_CARE":        return new String[]{"var(--d-selfcare)", "var(--c-violet-bg)", "bi-star"};
            case "MOTOR":            return new String[]{"var(--d-motor)",    "var(--c-cyan-bg)",   "bi-activity"};
            default:                 return new String[]{"var(--ink)",        "var(--bg)",          "bi-bullseye"};
        }
    }

    /**
     * Disability distribution as count rows with a proportional bar width
     * (relative to the largest group) — a list, not a pie chart.
     */
    private List<Map<String, Object>> disabilityRows(DashboardData data) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (data.getDisabilityDistribution() == null) {
            return rows;
        }
        long max = 0;
        for (com.school.analytics.CountRow r : data.getDisabilityDistribution()) {
            max = Math.max(max, r.getTotal());
        }
        for (com.school.analytics.CountRow r : data.getDisabilityDistribution()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("label", r.getLabel());
            m.put("total", r.getTotal());
            m.put("pct", max == 0 ? 0 : Math.round(r.getTotal() * 100f / max));
            rows.add(m);
        }
        return rows;
    }

    private List<Map<String, Object>> upcomingSessions(User user, long childId) {
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> out = new ArrayList<>();
        List<TherapySession> all = new ArrayList<>(therapy.forStudent(user, childId));
        all.sort((a, b) -> a.getStartTime().compareTo(b.getStartTime()));
        for (TherapySession s : all) {
            if (s.getStartTime() == null || s.getStartTime().isBefore(now)
                    || !"SCHEDULED".equals(s.getStatus())) {
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            String typeLabel = com.school.util.Lookups.label(s.getTherapyType());
            m.put("tag", typeLabel);
            m.put("title", typeLabel + " Therapy"
                    + (s.getTherapistName() == null ? "" : " with " + s.getTherapistName()));
            m.put("desc", s.getNotes() == null || s.getNotes().isBlank()
                    ? "Scheduled session." : s.getNotes());
            m.put("when", s.getStartTime().format(DateTimeFormatter.ofPattern("EEE · HH:mm", Locale.ENGLISH)));
            long mins = s.getEndTime() == null ? 0 : Duration.between(s.getStartTime(), s.getEndTime()).toMinutes();
            m.put("duration", mins > 0 ? mins + " min" : "");
            m.put("location", s.getLocation());
            m.put("initials", initials(s.getTherapistName()));
            out.add(m);
            if (out.size() == 2) {
                break;
            }
        }
        return out;
    }

    /**
     * Rolling 7-day strip that starts with today (today is always the first cell).
     * Each day is enriched with the real activities (therapy sessions + school
     * events, via the same {@link CalendarService} the calendar page uses), so a
     * dashboard can mark busy days and reveal what's on when a day is clicked.
     * {@code childId} may be null: for a parent that means no child linked (no
     * activities), and for staff it means the role-scoped (caseload / school-wide)
     * events for that window.
     */
    private List<Map<String, Object>> weekStrip(User user, Long childId, LocalDate today) {
        // Rolling 7-day window starting with today, so "today" is always the first cell.
        LocalDate start = today;

        // Bucket the window's combined events by date (yyyy-MM-dd). childId null is
        // valid here — CalendarService scopes the result to the user's role.
        Map<String, List<Map<String, Object>>> byDate = new HashMap<>();
        for (CalendarEvent e : calendar.events(user, childId, start, start.plusDays(7))) {
            if (e.getStart() == null || e.getStart().length() < 10) {
                continue;
            }
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("time", (e.isAllDay() || e.getStart().length() < 16)
                    ? "All day" : e.getStart().substring(11, 16));
            a.put("title", e.getTitle());
            a.put("color", e.getColor());
            a.put("sort", e.getStart());
            byDate.computeIfAbsent(e.getStart().substring(0, 10), k -> new ArrayList<>()).add(a);
        }

        List<Map<String, Object>> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = start.plusDays(i);
            List<Map<String, Object>> acts = byDate.getOrDefault(d.toString(), new ArrayList<>());
            acts.sort((x, y) -> ((String) x.get("sort")).compareTo((String) y.get("sort")));
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            m.put("num", d.getDayOfMonth());
            m.put("today", d.equals(today));
            m.put("full", d.format(DateTimeFormatter.ofPattern("EEEE, d MMM", Locale.ENGLISH)));
            m.put("activities", acts);
            days.add(m);
        }
        return days;
    }

    private static String firstName(String full) {
        if (full == null || full.isBlank()) {
            return "your child";
        }
        return full.trim().split("\\s+")[0];
    }

    private static String initials(String name) {
        if (name == null || name.isBlank()) {
            return "--";
        }
        String[] parts = name.trim().split("\\s+");
        String s = parts[0].substring(0, 1);
        if (parts.length > 1) {
            s += parts[parts.length - 1].substring(0, 1);
        }
        return s.toUpperCase();
    }
}
