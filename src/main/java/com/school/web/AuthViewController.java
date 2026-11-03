package com.school.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Serves the login page; the POST is handled by Spring Security. */
@Controller
public class AuthViewController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
