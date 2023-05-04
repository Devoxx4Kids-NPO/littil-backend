package org.littil.api.exception;

import lombok.extern.slf4j.Slf4j;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
@Slf4j
public class ServiceExceptionMapper extends AbstractExceptionMapper<ServiceException> {

    public ServiceExceptionMapper() {
        super(Response.Status.INTERNAL_SERVER_ERROR);
    }
}