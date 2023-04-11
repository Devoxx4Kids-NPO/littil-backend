package org.littil.api.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class AbstractExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {
    private final Response.Status status;

    protected AbstractExceptionMapper(Response.Status status) {
        this.status = status;
    }

    protected ErrorResponse build(T e) {
        ErrorResponse.ErrorMessage errorMessage = new ErrorResponse.ErrorMessage(e.getMessage());
        return new ErrorResponse(errorMessage);
    }

    @Override
    public Response toResponse(T e) {
        ErrorResponse body = build(e);
        return Response.status(this.status)
                .entity(body)
                .build();
    }
}
