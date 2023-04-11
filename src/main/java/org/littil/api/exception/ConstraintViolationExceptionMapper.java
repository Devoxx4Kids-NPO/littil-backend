package org.littil.api.exception;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.List;

@Provider
public class ConstraintViolationExceptionMapper extends AbstractExceptionMapper<ConstraintViolationException> {

    public ConstraintViolationExceptionMapper() {
        super(Response.Status.BAD_REQUEST);
    }

    @Override
    protected List<ErrorResponse.ErrorMessage> build(ConstraintViolationException e) {
        return e.getConstraintViolations()
                .stream()
                .map(constraintViolation -> new ErrorResponse.ErrorMessage(constraintViolation.getPropertyPath().toString(), constraintViolation.getMessage()))
                .toList();
    }
}