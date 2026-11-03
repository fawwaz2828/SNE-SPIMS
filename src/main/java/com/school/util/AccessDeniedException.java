package com.school.util;

/**
 * Thrown by the Service layer when an authenticated user attempts to act on data
 * outside their permitted scope (e.g. a teacher accessing an unassigned student).
 * Controllers translate this into an HTTP 403 / access-denied page.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
