package org.littil.api.auth.authz;


import lombok.NoArgsConstructor;
import org.littil.api.auth.service.AuthorizationType;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.ext.Provider;

@UserOwned(type = AuthorizationType.GUEST_TEACHER)
@Provider
@NoArgsConstructor
public class GuestTeacherSecurityInterceptor {

    AuthorizationType getAuthorizationType() {
        return AuthorizationType.GUEST_TEACHER;
    }
}
