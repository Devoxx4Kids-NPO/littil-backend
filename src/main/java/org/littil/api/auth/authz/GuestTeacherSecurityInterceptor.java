package org.littil.api.auth.authz;


import lombok.NoArgsConstructor;
import org.littil.api.auth.service.AuthorizationType;

import javax.inject.Named;
import javax.ws.rs.ext.Provider;

@GuestTeacherSecured
@Named
@Provider
@NoArgsConstructor
public class GuestTeacherSecurityInterceptor extends AbstractSecurityInterceptor {

    AuthorizationType getAuthorizationType() {
        return AuthorizationType.GUEST_TEACHER;
    }
}
