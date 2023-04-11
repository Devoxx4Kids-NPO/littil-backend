package org.littil.api.exception;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.ResourceBundle;
import java.util.stream.Stream;

@Provider
@Slf4j
public class ThrowableMapper extends AbstractExceptionMapper<Throwable> {

    public ThrowableMapper() {
        super(Response.Status.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected Stream<ErrorResponse.ErrorMessage> build(Throwable e) {
        String defaultErrorMessage = ResourceBundle.getBundle("ValidationMessages").getString("System.error");
        return Stream.of(new ErrorResponse.ErrorMessage(defaultErrorMessage));
    }
}