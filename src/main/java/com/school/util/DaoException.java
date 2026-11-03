package com.school.util;

/**
 * Wraps low-level {@link java.sql.SQLException}s thrown in the DAO layer so the
 * upper layers do not need to handle checked SQL exceptions everywhere.
 */
public class DaoException extends RuntimeException {

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }

    public DaoException(String message) {
        super(message);
    }
}
