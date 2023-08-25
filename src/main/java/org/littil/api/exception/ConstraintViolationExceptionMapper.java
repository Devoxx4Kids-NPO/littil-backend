package org.littil.api.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.util.stream.Stream;

@Provider
public class ConstraintViolationExceptionMapper extends AbstractExceptionMapper<ConstraintViolationException> {

    public ConstraintViolationExceptionMapper() {
        super(Response.Status.BAD_REQUEST);
    }

    @Override
    protected ErrorResponse build(ConstraintViolationException e) {
        Stream<ErrorResponse.ErrorMessage> messages = e.getConstraintViolations()
                .stream()
                .map(constraintViolation -> new ErrorResponse.ErrorMessage(constraintViolation.getPropertyPath().toString(), constraintViolation.getMessage()));
        // Responses without ErrorId's are logged as debug; ConstraintViolationException is validation feedback
        return new ErrorResponse(null,messages.toList());
    }
}