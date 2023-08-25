package org.littil.api.auth.authz;

import lombok.NoArgsConstructor;
import org.littil.api.auth.service.AuthorizationType;

import jakarta.inject.Named;
import jakarta.ws.rs.ext.Provider;

@SchoolSecured
@Named
@Provider
@NoArgsConstructor
public class SchoolSecurityInterceptor extends AbstractSecurityInterceptor {

    AuthorizationType getAuthorizationType() {
        return AuthorizationType.SCHOOL;
    }

}
