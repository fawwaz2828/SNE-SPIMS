package com.school.security;

import com.school.dao.UserDao;
import com.school.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Loads accounts for authentication from the existing {@code users} table. */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserDao userDao;

    public AppUserDetailsService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user: " + username));
        return new AppUserDetails(user);
    }
}
