package org.littil.api.exception;

public class EntityAlreadyExistsException extends RuntimeException {

    public EntityAlreadyExistsException() {
        super();
    }
    public EntityAlreadyExistsException(String message) {
        super(message);
    }
}
