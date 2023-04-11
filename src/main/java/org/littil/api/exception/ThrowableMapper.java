package org.littil.api.exception;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.List;
import java.util.ResourceBundle;

@Provider
@Slf4j
public class ThrowableMapper extends AbstractExceptionMapper<Throwable> {

    public ThrowableMapper() {
        super(Response.Status.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected List<ErrorResponse.ErrorMessage> build(Throwable e) {
        String defaultErrorMessage = ResourceBundle.getBundle("ValidationMessages").getString("System.error");
        return List.of(new ErrorResponse.ErrorMessage(defaultErrorMessage));
    }
}