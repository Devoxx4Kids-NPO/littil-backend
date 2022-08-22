package org.littil.api.auth.provider.auth0.exception;

import org.littil.api.auth.provider.AuthProviderException;

public class Auth0UserException extends AuthProviderException {

    public Auth0UserException(String message) {
        super(message);
    }

    public Auth0UserException(String message, Throwable cause) {
        super(message, cause);
    }
}
