package org.littil.api.exception;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class NotFoundExceptionMapper extends AbstractExceptionMapper<NotFoundException> {
    public NotFoundExceptionMapper() {
        super(Response.Status.NOT_FOUND);
    }
}
