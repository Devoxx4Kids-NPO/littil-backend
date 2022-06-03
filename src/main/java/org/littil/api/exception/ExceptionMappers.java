package org.littil.api.exception;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

public class ExceptionMappers {

    @ServerExceptionMapper
    public RestResponse<String> mapException(NotFoundException x) {
        return RestResponse.status(Response.Status.NOT_FOUND);
    }

}
