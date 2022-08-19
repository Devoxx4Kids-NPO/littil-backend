package org.littil.api.auth.provider.auth0.exception;

import org.littil.api.auth.provider.AuthProviderException;

public class Auth0AuthorizationException extends AuthProviderException {
    public Auth0AuthorizationException(String message) {
        super(message);
    }

    public Auth0AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
