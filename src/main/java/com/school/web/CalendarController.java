package com.school.web;

import com.school.calendar.CalendarEvent;
import com.school.calendar.CalendarService;
import com.school.model.User;
import com.school.security.SecurityUtils;
import com.school.service.StudentService;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

/** Unified calendar page + its FullCalendar JSON feed (Feature 5). */
@Controller
public class CalendarController {

    private final CalendarService calendar;
    private final StudentService students;

    public CalendarController(CalendarService calendar, StudentService students) {
        this.calendar = calendar;
        this.students = students;
    }

    @GetMapping("/calendar")
    public String page(@RequestParam(required = false) Long studentId, Model model) {
        User user = SecurityUtils.currentUser();
        model.addAttribute("visibleStudents", students.visibleRoster(user, null, null, null, null));
        model.addAttribute("selectedStudentId", studentId);
        return "calendar";
    }

    @GetMapping("/calendar/events")
    @ResponseBody
    public List<CalendarEvent> events(@RequestParam String start,
                                      @RequestParam String end,
                                      @RequestParam(required = false) Long studentId) {
        // FullCalendar sends ISO timestamps; take the date portion. Malformed
        // input (e.g. tampered query params) yields a clean 400, not a 500.
        LocalDate from = parseDate(start);
        LocalDate to = parseDate(end);
        return calendar.events(SecurityUtils.currentUser(), studentId, from, to);
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.length() < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date range.");
        }
        try {
            return LocalDate.parse(value.substring(0, 10));
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date range.");
        }
    }
}
