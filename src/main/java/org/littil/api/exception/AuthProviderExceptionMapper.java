package org.littil.api.exception;

import org.littil.api.auth.provider.AuthProviderException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AuthProviderExceptionMapper extends AbstractExceptionMapper<AuthProviderException> {

    public AuthProviderExceptionMapper() {
        super(Response.Status.INTERNAL_SERVER_ERROR);
    }
}