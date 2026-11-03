package com.school.dao;

import com.school.model.User;

import java.util.List;
import java.util.Optional;

/** Data access contract for system accounts. */
public interface UserDao {

    Optional<User> findById(long id);

    Optional<User> findByUsername(String username);

    /** All users, optionally filtered by role; newest first. */
    List<User> findAll(String roleFilter);

    /** Active users of a given role — used to populate assignment dropdowns. */
    List<User> findActiveByRole(String role);

    long insert(User user);

    void update(User user);

    void updatePassword(long userId, String passwordHash);

    void setActive(long userId, boolean active);

    boolean usernameExists(String username, long excludeId);

    boolean emailExists(String email, long excludeId);
}
