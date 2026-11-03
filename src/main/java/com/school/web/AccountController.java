package com.school.web;

import com.school.model.User;
import com.school.security.SecurityUtils;
import com.school.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Self-service account page: profile view and password change. */
@Controller
@RequestMapping("/account")
public class AccountController {

    private final AuthService auth;

    public AccountController(AuthService auth) {
        this.auth = auth;
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("user", SecurityUtils.currentUser());
        return "account";
    }

    @PostMapping("/password")
    public String changePassword(@RequestParam String current,
                                 @RequestParam String next,
                                 @RequestParam String confirm,
                                 RedirectAttributes ra) {
        User user = SecurityUtils.currentUser();
        auth.changePassword(user.getId(), current, next, confirm);
        ra.addFlashAttribute("flashSuccess", "Password changed.");
        return "redirect:/account";
    }
}
