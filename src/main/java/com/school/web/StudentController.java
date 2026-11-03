package com.school.web;

import com.school.model.Student;
import com.school.model.User;
import com.school.security.SecurityUtils;
import com.school.service.StudentService;
import com.school.service.UserService;
import com.school.util.AccessDeniedException;
import com.school.util.Lookups;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Student roster, profile CRUD (admin) and the per-student hub page that links to
 * every student-scoped feature (IEP, reports, incidents, therapy,
 * messaging). Reads are scoped to the caller's caseload by the service.
 */
@Controller
@RequestMapping("/students")
public class StudentController {

    private final StudentService students;
    private final UserService users;

    public StudentController(StudentService students, UserService users) {
        this.students = students;
        this.users = users;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String disability,
                       @RequestParam(required = false) String support,
                       @RequestParam(required = false) String classGroup,
                       @RequestParam(required = false) String keyword,
                       Model model) {
        User user = SecurityUtils.currentUser();
        model.addAttribute("students",
                students.visibleRoster(user, blankToNull(disability), blankToNull(support),
                        blankToNull(classGroup), blankToNull(keyword)));
        model.addAttribute("disabilityTypes", Lookups.DISABILITY_TYPES);
        model.addAttribute("supportLevels", Lookups.SUPPORT_LEVELS);
        model.addAttribute("disability", disability);
        model.addAttribute("support", support);
        model.addAttribute("classGroup", classGroup);
        model.addAttribute("keyword", keyword);
        model.addAttribute("canManage", user.isAdmin());
        return "students/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        requireAdmin();
        model.addAttribute("student", new Student());
        addFormLookups(model);
        return "students/form";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable long id, Model model) {
        User user = SecurityUtils.currentUser();
        model.addAttribute("student", students.get(user, id));
        model.addAttribute("canManage", user.isAdmin());
        model.addAttribute("isStaff", user.isAdmin() || user.isTeacher());
        return "students/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable long id, Model model) {
        User user = requireAdmin();
        model.addAttribute("student", students.get(user, id));
        addFormLookups(model);
        return "students/form";
    }

    @PostMapping
    public String save(@RequestParam(defaultValue = "0") long id,
                       @RequestParam String studentCode,
                       @RequestParam String fullName,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
                       @RequestParam String gender,
                       @RequestParam String disabilityType,
                       @RequestParam String supportLevel,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate enrollmentDate,
                       @RequestParam(required = false) String classGroup,
                       @RequestParam(required = false) String notes,
                       RedirectAttributes ra) {
        requireAdmin();
        Student s = new Student();
        s.setId(id);
        s.setStudentCode(studentCode);
        s.setFullName(fullName);
        s.setDateOfBirth(dateOfBirth);
        s.setGender(gender);
        s.setDisabilityType(disabilityType);
        s.setSupportLevel(supportLevel);
        s.setEnrollmentDate(enrollmentDate);
        s.setClassGroup(blankToNull(classGroup));
        s.setNotes(blankToNull(notes));
        long studentId = id;
        if (id == 0) {
            studentId = students.create(s);
            ra.addFlashAttribute("flashSuccess", "Student created.");
        } else {
            students.update(s);
            ra.addFlashAttribute("flashSuccess", "Student updated.");
        }
        return "redirect:/students/" + studentId;
    }

    @PostMapping("/{id}/active")
    public String setActive(@PathVariable long id, @RequestParam boolean active, RedirectAttributes ra) {
        requireAdmin();
        students.setActive(id, active);
        ra.addFlashAttribute("flashSuccess", active ? "Student reactivated." : "Student deactivated.");
        return "redirect:/students/" + id;
    }

    private void addFormLookups(Model model) {
        model.addAttribute("genders", Lookups.GENDERS);
        model.addAttribute("disabilityTypes", Lookups.DISABILITY_TYPES);
        model.addAttribute("supportLevels", Lookups.SUPPORT_LEVELS);
    }

    private User requireAdmin() {
        User u = SecurityUtils.currentUser();
        if (u == null || !u.isAdmin()) {
            throw new AccessDeniedException("Only administrators can manage student profiles.");
        }
        return u;
    }

    private static String blankToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}