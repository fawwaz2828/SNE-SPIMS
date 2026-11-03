package com.school.web;

import com.school.util.AccessDeniedException;
import com.school.util.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Renders the application's domain exceptions as friendly pages. */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String denied(AccessDeniedException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "common/denied";
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String validation(ValidationException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "common/message";
    }

}
