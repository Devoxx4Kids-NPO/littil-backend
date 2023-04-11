package org.littil.api.exception;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.List;
import java.util.UUID;

@Slf4j
public class AbstractExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {
    private final Response.Status status;

    protected AbstractExceptionMapper(Response.Status status) {
        this.status = status;
    }

    protected List<ErrorResponse.ErrorMessage> build(T e) {
        return List.of(new ErrorResponse.ErrorMessage(e.getMessage()));
    }

    @Override
    public Response toResponse(T e) {
        ErrorResponse body = new ErrorResponse(UUID.randomUUID().toString(),build(e));
        log.error("error[{},{}] errorId: {}",this.status,e.getClass().getSimpleName(), body.getErrorId(), e);
        return Response.status(this.status)
                .entity(body)
                .build();
    }
}
