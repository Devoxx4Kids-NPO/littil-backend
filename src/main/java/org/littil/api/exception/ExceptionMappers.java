package org.littil.api.exception;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.littil.api.userSetting.UserSettingNotFound;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

public class ExceptionMappers {

    @ServerExceptionMapper
    public RestResponse<String> mapException(NotFoundException x) {
        return RestResponse.status(Response.Status.NOT_FOUND);
    }

    @ServerExceptionMapper
    public RestResponse<String> mapException(UserSettingNotFound x) {
        return RestResponse.status(RestResponse.Status.NOT_FOUND);
    }

}
