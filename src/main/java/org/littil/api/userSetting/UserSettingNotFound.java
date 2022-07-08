package org.littil.api.userSetting;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;

public class UserSettingNotFound extends ClientErrorException {

    public UserSettingNotFound() {
        super(Response.Status.NOT_FOUND);
    }

    public UserSettingNotFound(String message) {
        super(message, Response.Status.NOT_FOUND);
    }
}
