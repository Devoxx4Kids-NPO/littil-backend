package org.littil.api.exception;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotFoundExceptionMapper extends AbstractExceptionMapper<NotFoundException> {
    public NotFoundExceptionMapper() {
        super(Response.Status.NOT_FOUND);
    }
}
