package org.example.exception;

/**
 * Exception thrown when database connection fails.
 * Used during initialization or when connection pool encounters errors.
 */
public class DatabaseConnectionException extends RuntimeException {

    public DatabaseConnectionException(String message) {
        super(message);
    }

    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
