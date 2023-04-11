package org.littil.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class AbstractExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {
    private final Response.Status status;
    private final boolean generateId;
    private final Logger log;

    protected AbstractExceptionMapper(Response.Status status) {
        this(status,true);

    }
    protected AbstractExceptionMapper(Response.Status status, boolean generateId) {
        this.log = LoggerFactory.getLogger(getClass());
        this.status = status;
        this.generateId = generateId;
    }

    protected Stream<ErrorResponse.ErrorMessage> build(T e) {
        return Stream.of(new ErrorResponse.ErrorMessage(e.getMessage()));
    }

    @Override
    public Response toResponse(T e) {
        Optional<UUID> errorId = this.generateId?Optional.of(UUID.randomUUID()):Optional.empty();
        String exceptionType = e.getClass().getSimpleName();
        errorId.ifPresentOrElse(
                id -> log.error("mapping {} to {}, errorId: {}",exceptionType,this.status, errorId, e),
                () -> log.debug("mapping {} to {}",exceptionType,this.status,e));
        Stream<ErrorResponse.ErrorMessage> messages = build(e);
        return Response.status(this.status)
                .entity(new ErrorResponse(errorId.map(Objects::toString).orElse(null),messages.toList()))
                .build();
    }
}
