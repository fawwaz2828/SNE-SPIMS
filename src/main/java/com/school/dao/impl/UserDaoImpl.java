package com.school.dao.impl;

import com.school.dao.UserDao;
import com.school.model.User;
import com.school.util.MyBatis;

import java.util.List;
import java.util.Optional;

/** MyBatis-backed {@link UserDao}; SQL lives in {@code mappers/UserMapper.xml}. */
public class UserDaoImpl implements UserDao {

    @Override
    public Optional<User> findById(long id) {
        return MyBatis.selectOne("UserMapper.findById", id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return MyBatis.selectOne("UserMapper.findByUsername", username);
    }

    @Override
    public List<User> findAll(String roleFilter) {
        return MyBatis.selectList("UserMapper.findAll", MyBatis.args("role", roleFilter));
    }

    @Override
    public List<User> findActiveByRole(String role) {
        return MyBatis.selectList("UserMapper.findActiveByRole", role);
    }

    @Override
    public long insert(User u) {
        MyBatis.insert("UserMapper.insert", u);
        return u.getId();
    }

    @Override
    public void update(User u) {
        MyBatis.update("UserMapper.update", u);
    }

    @Override
    public void updatePassword(long userId, String passwordHash) {
        MyBatis.update("UserMapper.updatePassword",
                MyBatis.args("userId", userId, "passwordHash", passwordHash));
    }

    @Override
    public void setActive(long userId, boolean active) {
        MyBatis.update("UserMapper.setActive", MyBatis.args("userId", userId, "active", active));
    }

    @Override
    public boolean usernameExists(String username, long excludeId) {
        return MyBatis.count("UserMapper.usernameExists",
                MyBatis.args("username", username, "excludeId", excludeId)) > 0;
    }

    @Override
    public boolean emailExists(String email, long excludeId) {
        return MyBatis.count("UserMapper.emailExists",
                MyBatis.args("email", email, "excludeId", excludeId)) > 0;
    }
}