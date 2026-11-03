package com.school.web;

import com.school.model.User;
import com.school.security.SecurityUtils;
import com.school.service.UserService;
import com.school.util.AccessDeniedException;
import com.school.util.Lookups;
import com.school.util.ValidationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Admin-only account management: list, create, edit, reset password, activate. */
@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String role, Model model) {
        requireAdmin();
        model.addAttribute("users", users.list(blankToNull(role)));
        model.addAttribute("roles", Lookups.ROLES);
        model.addAttribute("roleFilter", role);
        return "users/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        requireAdmin();
        model.addAttribute("user", new User());
        model.addAttribute("roles", Lookups.ROLES);
        return "users/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable long id, Model model) {
        requireAdmin();
        model.addAttribute("user", users.find(id)
                .orElseThrow(() -> new ValidationException("User not found.")));
        model.addAttribute("roles", Lookups.ROLES);
        return "users/form";
    }

    @PostMapping
    public String save(@RequestParam(defaultValue = "0") long id,
                       @RequestParam String username,
                       @RequestParam String fullName,
                       @RequestParam String email,
                       @RequestParam(required = false) String phone,
                       @RequestParam String role,
                       @RequestParam(required = false) String password,
                       RedirectAttributes ra) {
        requireAdmin();
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPhone(blankToNull(phone));
        u.setRole(role);
        if (id == 0) {
            users.create(u, password);
            ra.addFlashAttribute("flashSuccess", "Account created.");
        } else {
            users.update(u);
            ra.addFlashAttribute("flashSuccess", "Account updated.");
        }
        return "redirect:/users";
    }

    @PostMapping("/{id}/password")
    public String resetPassword(@PathVariable long id, @RequestParam String password, RedirectAttributes ra) {
        requireAdmin();
        users.resetPassword(id, password);
        ra.addFlashAttribute("flashSuccess", "Password reset.");
        return "redirect:/users";
    }

    @PostMapping("/{id}/active")
    public String setActive(@PathVariable long id, @RequestParam boolean active, RedirectAttributes ra) {
        requireAdmin();
        users.setActive(id, active);
        ra.addFlashAttribute("flashSuccess", active ? "Account reactivated." : "Account deactivated.");
        return "redirect:/users";
    }

    private void requireAdmin() {
        User u = SecurityUtils.currentUser();
        if (u == null || !u.isAdmin()) {
            throw new AccessDeniedException("Only administrators can manage accounts.");
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}
