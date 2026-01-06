package org.example.exception;

/**
 * Exception thrown when attempting to create duplicate data.
 * For example, when trying to create a user with an existing username.
 */
public class DuplicateDataException extends DAOException {

    public DuplicateDataException(String message) {
        super(message);
    }

    public DuplicateDataException(String field, String value) {
        super(field + " already exists: " + value);
    }
}
