package org.littil.api.exception;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
public class AbstractExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {
    private final Response.Status status;

    protected AbstractExceptionMapper(Response.Status status) {
        this.status = status;
    }

    protected Stream<ErrorResponse.ErrorMessage> build(T e) {
        return Stream.of(new ErrorResponse.ErrorMessage(e.getMessage()));
    }

    @Override
    public Response toResponse(T e) {
        UUID errorId = UUID.randomUUID();
        log.error("error[{},{}] errorId: {}",this.status,e.getClass().getSimpleName(), errorId, e);
        Stream<ErrorResponse.ErrorMessage> messages = build(e);
        return Response.status(this.status)
                .entity(new ErrorResponse(errorId.toString(),messages.toList()))
                .build();
    }
}
