package com.school.web;

import com.school.model.IepGoal;
import com.school.model.IepProgram;
import com.school.model.User;
import com.school.security.SecurityUtils;
import com.school.service.IepService;
import com.school.service.StudentService;
import com.school.util.AccessDeniedException;
import com.school.util.Lookups;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** IEP programs and goals. Staff edit; parents read-only. */
@Controller
public class IepController {

    private final IepService iep;
    private final StudentService students;

    public IepController(IepService iep, StudentService students) {
        this.iep = iep;
        this.students = students;
    }

    @GetMapping("/students/{studentId}/iep")
    public String list(@PathVariable long studentId, Model model) {
        User user = SecurityUtils.currentUser();
        model.addAttribute("student", students.get(user, studentId));
        model.addAttribute("programs", iep.programsForStudent(user, studentId));
        model.addAttribute("canEdit", isStaff(user));
        return "iep/list";
    }

    @GetMapping("/students/{studentId}/iep/new")
    public String newForm(@PathVariable long studentId, Model model) {
        User user = SecurityUtils.currentUser();
        requireStaff(user);
        model.addAttribute("student", students.get(user, studentId));
        IepProgram p = new IepProgram();
        p.setStudentId(studentId);
        p.setStatus("DRAFT");
        model.addAttribute("program", p);
        model.addAttribute("statuses", Lookups.IEP_STATUS);
        return "iep/form";
    }

    @PostMapping("/students/{studentId}/iep")
    public String create(@PathVariable long studentId,
                         @RequestParam String title,
                         @RequestParam String academicYear,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                         @RequestParam String status,
                         @RequestParam(required = false) String presentLevels,
                         @RequestParam(required = false) String accommodations,
                         RedirectAttributes ra) {
        User user = SecurityUtils.currentUser();
        requireStaff(user);
        students.get(user, studentId); // access scope check
        IepProgram p = new IepProgram();
        p.setStudentId(studentId);
        p.setTitle(title);
        p.setAcademicYear(academicYear);
        p.setStartDate(startDate);
        p.setEndDate(endDate);
        p.setPresentLevels(presentLevels);
        p.setAccommodations(accommodations);
        p.setStatus(status);
        p.setCreatedBy(user.getId());
        long id = iep.createProgram(p);
        ra.addFlashAttribute("flashSuccess", "IEP program created.");
        return "redirect:/iep/" + id;
    }

    @GetMapping("/iep/{programId}")
    public String detail(@PathVariable long programId, Model model) {
        User user = SecurityUtils.currentUser();
        IepProgram program = iep.getProgram(user, programId);
        model.addAttribute("program", program);
        model.addAttribute("goalsByDomain", iep.goalsByDomain(program));
        model.addAttribute("domains", Lookups.DOMAINS);
        model.addAttribute("goalStatuses", Lookups.GOAL_STATUS);
        model.addAttribute("canEdit", isStaff(user));
        // Short labels (goalId -> description) for the progress trend chart.
        Map<Long, String> goalLabels = new java.util.LinkedHashMap<>();
        for (com.school.model.IepGoal g : program.getGoals()) {
            goalLabels.put(g.getId(), g.getDescription());
        }
        model.addAttribute("goalLabels", goalLabels);
        return "iep/detail";
    }

    @GetMapping("/iep/{programId}/edit")
    public String editForm(@PathVariable long programId, Model model) {
        User user = SecurityUtils.currentUser();
        requireStaff(user);
        IepProgram program = iep.getProgram(user, programId);
        model.addAttribute("student", students.get(user, program.getStudentId()));
        model.addAttribute("program", program);
        model.addAttribute("statuses", Lookups.IEP_STATUS);
        return "iep/form";
    }

    @PostMapping("/iep/{programId}")
    public String update(@PathVariable long programId,
                         @RequestParam String title,
                         @RequestParam String academicYear,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                         @RequestParam String status,
                         @RequestParam(required = false) String presentLevels,
                         @RequestParam(required = false) String accommodations,
                         RedirectAttributes ra) {
        User user = SecurityUtils.currentUser();
        requireStaff(user);
        IepProgram existing = iep.getProgram(user, programId); // access check
        existing.setTitle(title);
        existing.setAcademicYear(academicYear);
        existing.setStartDate(startDate);
        existing.setEndDate(endDate);
        existing.setPresentLevels(presentLevels);
        existing.setAccommodations(accommodations);
        existing.setStatus(status);
        iep.updateProgram(existing);
        ra.addFlashAttribute("flashSuccess", "IEP program updated.");
        return "redirect:/iep/" + programId;
    }

    @PostMapping("/iep/{programId}/goals")
    public String addGoal(@PathVariable long programId,
                          @RequestParam String domain,
                          @RequestParam String description,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate,
                          @RequestParam String status,
                          @RequestParam(defaultValue = "0") int progressPct,
                          RedirectAttributes ra) {
        User user = SecurityUtils.currentUser();
        requireStaff(user);
        iep.getProgram(user, programId); // access check
        IepGoal g = new IepGoal();
        g.setIepId(programId);
        g.setDomain(domain);
        g.setDescription(description);
        g.setTargetDate(targetDate);
        g.setStatus(status);
        g.setProgressPct(progressPct);
        iep.addGoal(user, g);
        ra.addFlashAttribute("flashSuccess", "Goal added.");
        return "redirect:/iep/" + programId;
    }

    @PostMapping("/iep/{programId}/goals/{goalId}/update")
    public String updateGoal(@PathVariable long programId, @PathVariable long goalId,
                             @RequestParam String description,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate,
                             @RequestParam String status,
                             @RequestParam(defaultValue = "0") int progressPct,
                             RedirectAttributes ra) {
        User user = SecurityUtils.currentUser();
        requireStaff(user);
        IepGoal g = iep.getGoal(goalId);
        // Authorize against the goal's OWN program (prevents IDOR via a mismatched path programId).
        iep.getProgram(user, g.getIepId());
        if (g.getIepId() != programId) {
            throw new AccessDeniedException("Goal does not belong to this IEP.");
        }
        g.setDescription(description);
        g.setTargetDate(targetDate);
        g.setStatus(status);
        g.setProgressPct(progressPct);
        iep.updateGoal(user, g);
        ra.addFlashAttribute("flashSuccess", "Goal updated.");
        return "redirect:/iep/" + programId;
    }

    @PostMapping("/iep/{programId}/goals/{goalId}/delete")
    public String deleteGoal(@PathVariable long programId, @PathVariable long goalId, RedirectAttributes ra) {
        User user = SecurityUtils.currentUser();
        requireStaff(user);
        IepGoal g = iep.getGoal(goalId);
        // Authorize against the goal's OWN program (prevents IDOR via a mismatched path programId).
        iep.getProgram(user, g.getIepId());
        if (g.getIepId() != programId) {
            throw new AccessDeniedException("Goal does not belong to this IEP.");
        }
        iep.deleteGoal(goalId);
        ra.addFlashAttribute("flashSuccess", "Goal removed.");
        return "redirect:/iep/" + programId;
    }

    private boolean isStaff(User u) {
        return u != null && (u.isAdmin() || u.isTeacher());
    }

    private void requireStaff(User u) {
        if (!isStaff(u)) {
            throw new AccessDeniedException("Only teachers and admins can edit IEPs.");
        }
    }
}
