package org.littil.api.auth.authz;

import io.quarkus.arc.Priority;
import io.quarkus.oidc.IdToken;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Priority(3000)
@Provider
@UserOwned
public class UserSettingInterceptor implements ContainerRequestFilter {

    @IdToken
    Jwt jwt;

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        //if !( jwt.getUserID ()  user check in token voor dit request) {
            ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }
}
