package com.school.service;

import com.school.dao.UserDao;
import com.school.model.User;
import com.school.util.Lookups;
import com.school.util.Passwords;
import com.school.util.ValidationException;

import java.util.List;
import java.util.Optional;

/** Admin-facing account management with validation enforced before persistence. */
public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public List<User> list(String roleFilter) {
        return userDao.findAll(roleFilter);
    }

    public List<User> activeByRole(String role) {
        return userDao.findActiveByRole(role);
    }

    public Optional<User> find(long id) {
        return userDao.findById(id);
    }

    /** Creates a new account. {@code rawPassword} is hashed before storage. */
    public long create(User u, String rawPassword) {
        validateCommon(u);
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters.");
        }
        if (userDao.usernameExists(u.getUsername(), 0)) {
            throw new ValidationException("Username '" + u.getUsername() + "' is already taken.");
        }
        if (userDao.emailExists(u.getEmail(), 0)) {
            throw new ValidationException("Email '" + u.getEmail() + "' is already registered.");
        }
        u.setPasswordHash(Passwords.hash(rawPassword));
        return userDao.insert(u);
    }

    public void update(User u) {
        validateCommon(u);
        if (userDao.emailExists(u.getEmail(), u.getId())) {
            throw new ValidationException("Email '" + u.getEmail() + "' is already registered.");
        }
        userDao.update(u);
    }

    public void resetPassword(long userId, String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters.");
        }
        userDao.updatePassword(userId, Passwords.hash(rawPassword));
    }

    public void setActive(long userId, boolean active) {
        userDao.setActive(userId, active);
    }

    private void validateCommon(User u) {
        if (isBlank(u.getUsername())) {
            throw new ValidationException("Username is required.");
        }
        if (isBlank(u.getFullName())) {
            throw new ValidationException("Full name is required.");
        }
        if (isBlank(u.getEmail()) || !u.getEmail().contains("@")) {
            throw new ValidationException("A valid email is required.");
        }
        if (!Lookups.ROLES.contains(u.getRole())) {
            throw new ValidationException("Invalid role.");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
