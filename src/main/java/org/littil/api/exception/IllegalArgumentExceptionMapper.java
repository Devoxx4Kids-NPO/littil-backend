package org.littil.api.exception;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class IllegalArgumentExceptionMapper extends AbstractExceptionMapper<IllegalArgumentException> {
    public IllegalArgumentExceptionMapper() {
        super(Response.Status.CONFLICT);
    }
}
