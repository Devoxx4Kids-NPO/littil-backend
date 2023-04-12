package org.littil.api.exception;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
@Slf4j
public class ServiceExceptionMapper extends AbstractExceptionMapper<ServiceException> {

    public ServiceExceptionMapper() {
        super(Response.Status.INTERNAL_SERVER_ERROR);
    }
}