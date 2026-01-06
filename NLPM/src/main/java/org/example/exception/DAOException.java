package org.example.exception;

/**
 * Base exception for all DAO operations.
 * All other DAO-related exceptions extend this class.
 */
public class DAOException extends RuntimeException {

    public DAOException(String message) {
        super(message);
    }

    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }
}
