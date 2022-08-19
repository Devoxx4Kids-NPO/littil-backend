package org.littil.api.auth.provider.auth0.exception;

import org.littil.api.auth.provider.AuthProviderException;

public class Auth0RoleException extends AuthProviderException {

    public Auth0RoleException(String message) {
        super(message);
    }

    public Auth0RoleException(String message, Throwable cause) {
        super(message, cause);
    }
}
