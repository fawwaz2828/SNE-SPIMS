package com.school.util;

/**
 * Thrown by the Service layer when business validation fails (e.g. duplicate
 * username, schedule conflict, missing required field). The message is safe to
 * display directly to the end user; controllers surface it on the form.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
