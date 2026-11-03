package com.school.web;

import com.school.model.Announcement;
import com.school.model.User;
import com.school.security.SecurityUtils;
import com.school.service.AnnouncementService;
import com.school.util.AccessDeniedException;
import com.school.util.Lookups;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Admin-only announcement management. */
@Controller
@RequestMapping("/announcements")
public class AnnouncementController {

    private final AnnouncementService announcements;

    public AnnouncementController(AnnouncementService announcements) {
        this.announcements = announcements;
    }

    @GetMapping
    public String list(Model model) {
        requireAdmin();
        model.addAttribute("announcements", announcements.all());
        return "announcements/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        requireAdmin();
        model.addAttribute("announcement", new Announcement());
        model.addAttribute("targets", Lookups.ANNOUNCEMENT_TARGETS);
        return "announcements/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable long id, Model model) {
        requireAdmin();
        model.addAttribute("announcement", announcements.get(id));
        model.addAttribute("targets", Lookups.ANNOUNCEMENT_TARGETS);
        return "announcements/form";
    }

    @PostMapping
    public String save(@RequestParam(defaultValue = "0") long id,
                       @RequestParam String title,
                       @RequestParam String body,
                       @RequestParam String targetRole,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime publishAt,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expiresAt,
                       RedirectAttributes ra) {
        User user = requireAdmin();
        Announcement a = (id == 0) ? new Announcement() : announcements.get(id);
        a.setTitle(title);
        a.setBody(body);
        a.setTargetRole(targetRole);
        // Empty "publish at" means publish immediately, not "never".
        a.setPublishAt(publishAt != null ? publishAt : LocalDateTime.now());
        a.setExpiresAt(expiresAt);
        if (id == 0) {
            a.setCreatedBy(user.getId());
            announcements.create(a);
            ra.addFlashAttribute("flashSuccess", "Announcement published.");
        } else {
            announcements.update(a);
            ra.addFlashAttribute("flashSuccess", "Announcement updated.");
        }
        return "redirect:/announcements";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable long id, @RequestParam boolean active, RedirectAttributes ra) {
        requireAdmin();
        announcements.setActive(id, active);
        ra.addFlashAttribute("flashSuccess", active ? "Announcement activated." : "Announcement hidden.");
        return "redirect:/announcements";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable long id, RedirectAttributes ra) {
        requireAdmin();
        announcements.delete(id);
        ra.addFlashAttribute("flashSuccess", "Announcement deleted.");
        return "redirect:/announcements";
    }

    private User requireAdmin() {
        User u = SecurityUtils.currentUser();
        if (u == null || !u.isAdmin()) {
            throw new AccessDeniedException("Only administrators can manage announcements.");
        }
        return u;
    }
}
