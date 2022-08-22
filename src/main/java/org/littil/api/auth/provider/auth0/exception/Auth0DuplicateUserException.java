package org.littil.api.auth.provider.auth0.exception;

import org.littil.api.auth.provider.AuthProviderException;

public class Auth0DuplicateUserException extends AuthProviderException {

    public Auth0DuplicateUserException(String message) {
        super(message);
    }
}
