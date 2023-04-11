package org.littil.api.exception;

import org.littil.api.auth.provider.AuthProviderException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class AuthProviderExceptionMapper extends AbstractExceptionMapper<AuthProviderException> {

    public AuthProviderExceptionMapper() {
        super(Response.Status.INTERNAL_SERVER_ERROR);
    }
}