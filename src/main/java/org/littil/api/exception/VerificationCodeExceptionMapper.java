package org.littil.api.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class VerificationCodeExceptionMapper extends AbstractExceptionMapper<VerificationCodeException> {
    public VerificationCodeExceptionMapper() {
        super(Response.Status.CONFLICT);
    }
}
