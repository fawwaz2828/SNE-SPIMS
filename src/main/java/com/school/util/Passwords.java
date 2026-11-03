package com.school.util;

import org.mindrot.jbcrypt.BCrypt;

/** BCrypt password hashing helper used by the authentication service. */
public final class Passwords {

    private Passwords() {
    }

    public static String hash(String raw) {
        return BCrypt.hashpw(raw, BCrypt.gensalt(10));
    }

    public static boolean matches(String raw, String hash) {
        if (raw == null || hash == null || hash.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(raw, hash);
        } catch (IllegalArgumentException ex) {
            // Malformed stored hash
            return false;
        }
    }
}
