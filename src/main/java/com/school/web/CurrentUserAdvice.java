package com.school.web;

import com.school.model.User;
import com.school.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Injects the authenticated {@link User} and a few shared values into the model
 * of every view, so templates can reference {@code ${currentUser}} and
 * {@code ${schoolName}} without each controller repeating the work.
 */
@ControllerAdvice
public class CurrentUserAdvice {

    @Value("${app.school-name}")
    private String schoolName;

    @ModelAttribute("currentUser")
    public User currentUser() {
        return SecurityUtils.currentUser();
    }

    @ModelAttribute("schoolName")
    public String schoolName() {
        return schoolName;
    }

    /**
     * Current request path, used by the layout's icon rail to highlight the
     * active nav item. Exposed here because Thymeleaf 3.1 removed the
     * {@code #httpServletRequest} expression utility object.
     */
    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
