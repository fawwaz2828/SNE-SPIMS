package com.school.security;

import com.school.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Convenience accessor for the authenticated {@link User} from any layer. */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** The current domain user, or {@code null} when unauthenticated. */
    public static User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUserDetails details) {
            return details.getUser();
        }
        return null;
    }
}
