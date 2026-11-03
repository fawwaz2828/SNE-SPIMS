package com.school.service;

import com.school.dao.UserDao;
import com.school.model.User;
import com.school.util.Passwords;
import com.school.util.ValidationException;

import java.util.Optional;

/** Authentication: credential verification and self-service password change. */
public class AuthService {

    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Verifies credentials. Returns the authenticated {@link User} on success.
     * Throws {@link ValidationException} (safe message) on any failure so the
     * controller can re-display the login form.
     */
    public User authenticate(String username, String rawPassword) {
        if (username == null || username.trim().isEmpty()
                || rawPassword == null || rawPassword.isEmpty()) {
            throw new ValidationException("Please enter both username and password.");
        }
        Optional<User> found = userDao.findByUsername(username.trim());
        if (!found.isPresent()) {
            throw new ValidationException("Invalid username or password.");
        }
        User user = found.get();
        if (!user.isActive()) {
            throw new ValidationException("This account has been deactivated.");
        }
        if (!Passwords.matches(rawPassword, user.getPasswordHash())) {
            throw new ValidationException("Invalid username or password.");
        }
        return user;
    }

    public void changePassword(long userId, String current, String next, String confirm) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new ValidationException("Account not found."));
        if (!Passwords.matches(current, user.getPasswordHash())) {
            throw new ValidationException("Current password is incorrect.");
        }
        if (next == null || next.length() < 6) {
            throw new ValidationException("New password must be at least 6 characters.");
        }
        if (!next.equals(confirm)) {
            throw new ValidationException("New password and confirmation do not match.");
        }
        userDao.updatePassword(userId, Passwords.hash(next));
    }
}
