package org.littil.api.exception;

public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Exception cause) {
        super(message, cause);
    }

}
