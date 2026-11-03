package com.school.web;

import com.school.notify.NotificationService;
import com.school.security.SecurityUtils;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/** Notification inbox + the badge-count endpoint polled by the navbar bell. */
@Controller
public class NotificationController {

    private final NotificationService notifications;

    public NotificationController(NotificationService notifications) {
        this.notifications = notifications;
    }

    @GetMapping("/notifications")
    public String inbox(@RequestParam(defaultValue = "false") boolean archived, Model model) {
        long uid = SecurityUtils.currentUser().getId();
        model.addAttribute("notifications", notifications.inbox(uid, archived));
        model.addAttribute("showArchived", archived);
        model.addAttribute("unread", notifications.unreadCount(uid));
        return "notifications";
    }

    /** Polled by the bell every 30s. */
    @GetMapping("/notifications/unread-count")
    @ResponseBody
    public Map<String, Long> unreadCount() {
        return Map.of("count", notifications.unreadCount(SecurityUtils.currentUser().getId()));
    }

    @PostMapping("/notifications/{id}/read")
    public String markRead(@PathVariable long id) {
        notifications.markRead(id, SecurityUtils.currentUser().getId());
        return "redirect:/notifications";
    }

    @PostMapping("/notifications/read-all")
    public String markAllRead() {
        notifications.markAllRead(SecurityUtils.currentUser().getId());
        return "redirect:/notifications";
    }

    @PostMapping("/notifications/{id}/archive")
    public String archive(@PathVariable long id) {
        notifications.archive(id, SecurityUtils.currentUser().getId());
        return "redirect:/notifications";
    }
}
