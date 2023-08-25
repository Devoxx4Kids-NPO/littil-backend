package org.littil.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AbstractExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {
    private final Logger log;
    private final Response.Status status;
    protected AbstractExceptionMapper(Response.Status status) {
        this.log = LoggerFactory.getLogger(getClass());
        this.status = status;
    }

    protected ErrorResponse build(T e) {
        return new ErrorResponse(UUID.randomUUID().toString(), List.of(new ErrorResponse.ErrorMessage(e.getMessage())));
    }

    @Override
    public Response toResponse(T e) {
        ErrorResponse body = build(e);
        Optional.ofNullable(body.getErrorId())
                .ifPresentOrElse(
                id -> log.error("mapping {}, errorId: {}",this.status.getStatusCode(), id, e),
                () -> log.debug("mapping {}",this.status.getStatusCode(),e));
        return Response.status(this.status)
                .entity(body)
                .build();
    }
}
