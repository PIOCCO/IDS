package org.example.exception;

/**
 * Exception thrown when requested data is not found in the database.
 */
public class DataNotFoundException extends DAOException {

    public DataNotFoundException(String message) {
        super(message);
    }

    public DataNotFoundException(String entityType, Object id) {
        super(entityType + " not found with ID: " + id);
    }
}
