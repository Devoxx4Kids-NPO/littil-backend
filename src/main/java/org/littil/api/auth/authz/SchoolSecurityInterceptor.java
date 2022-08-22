package org.littil.api.auth.authz;

import lombok.NoArgsConstructor;
import org.littil.api.auth.service.AuthorizationType;

import javax.inject.Named;
import javax.ws.rs.ext.Provider;

@SchoolSecured
@Named
@Provider
@NoArgsConstructor
public class SchoolSecurityInterceptor extends AbstractSecurityInterceptor {

    AuthorizationType getAuthorizationType() {
        return AuthorizationType.SCHOOL;
    }

}
