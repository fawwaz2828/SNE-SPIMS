package com.school.web;

import com.school.model.User;
import com.school.security.SecurityUtils;
import com.school.service.StudentService;
import com.school.service.UserService;
import com.school.util.AccessDeniedException;
import com.school.util.Lookups;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Admin-only management of teacher/parent links for a student. */
@Controller
@RequestMapping("/students/{studentId}/assignments")
public class AssignmentController {

    private final StudentService students;
    private final UserService users;

    public AssignmentController(StudentService students, UserService users) {
        this.students = students;
        this.users = users;
    }

    @GetMapping
    public String page(@PathVariable long studentId, Model model) {
        User admin = requireAdmin();
        model.addAttribute("student", students.get(admin, studentId));
        model.addAttribute("teacherLinks", students.teacherLinks(studentId));
        model.addAttribute("parentLinks", students.parentLinks(studentId));
        model.addAttribute("teachers", users.activeByRole("TEACHER"));
        model.addAttribute("parents", users.activeByRole("PARENT"));
        model.addAttribute("relationships", Lookups.RELATIONSHIPS);
        return "students/assignments";
    }

    @PostMapping("/teacher")
    public String addTeacher(@PathVariable long studentId,
                             @RequestParam long teacherId,
                             @RequestParam(defaultValue = "false") boolean primary,
                             RedirectAttributes ra) {
        requireAdmin();
        students.assignTeacher(teacherId, studentId, primary);
        ra.addFlashAttribute("flashSuccess", "Teacher assigned.");
        return "redirect:/students/" + studentId + "/assignments";
    }

    @PostMapping("/teacher/{assignmentId}/remove")
    public String removeTeacher(@PathVariable long studentId, @PathVariable long assignmentId,
                                RedirectAttributes ra) {
        requireAdmin();
        students.removeTeacherLink(assignmentId);
        ra.addFlashAttribute("flashSuccess", "Teacher unassigned.");
        return "redirect:/students/" + studentId + "/assignments";
    }

    @PostMapping("/parent")
    public String addParent(@PathVariable long studentId,
                            @RequestParam long parentId,
                            @RequestParam String relationship,
                            RedirectAttributes ra) {
        requireAdmin();
        students.assignParent(parentId, studentId, relationship);
        ra.addFlashAttribute("flashSuccess", "Parent linked.");
        return "redirect:/students/" + studentId + "/assignments";
    }

    @PostMapping("/parent/{assignmentId}/remove")
    public String removeParent(@PathVariable long studentId, @PathVariable long assignmentId,
                               RedirectAttributes ra) {
        requireAdmin();
        students.removeParentLink(assignmentId);
        ra.addFlashAttribute("flashSuccess", "Parent unlinked.");
        return "redirect:/students/" + studentId + "/assignments";
    }

    private User requireAdmin() {
        User u = SecurityUtils.currentUser();
        if (u == null || !u.isAdmin()) {
            throw new AccessDeniedException("Only administrators can manage assignments.");
        }
        return u;
    }
}
