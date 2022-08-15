package org.littil.api.auth.authz;

import io.quarkus.arc.Priority;
import io.quarkus.security.UnauthorizedException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.littil.api.auth.service.AuthorizationType;
import org.littil.api.user.service.UserService;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.littil.api.Util.AUTHORIZATIONS_TOKEN_CLAIM;

@Priority(3000)
@Provider
@Slf4j
@AllArgsConstructor(onConstructor_ =  {@Inject})
@UserOwned(type = AuthorizationType.UNKNOWN)
@NoArgsConstructor
public abstract class AbstractSecurityInterceptor implements ContainerRequestFilter {
    JsonWebToken jwt;

    UserService userService;

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {

        if (ctx.getMethod().equals("GET")){
            // GET methods are public, so always allowed
             return;
        }

        MultivaluedMap<String, String> parameters = ctx.getUriInfo().getPathParameters();
        List<String> resourceIds = parameters.get("id");
        if (resourceIds == null) {
            throw new IllegalArgumentException("Could not find which resource this API is trying to access.");
        }
        if (resourceIds.size() != 1) {
            throw new IllegalArgumentException("Whoops we dit not expect this amount of parameters");
        }
        Optional<String> resourceId = resourceIds.stream().findFirst();
        Map<String, List<UUID>> authorizations = jwt.getClaim(AUTHORIZATIONS_TOKEN_CLAIM);

        checkIfAuthorized(UUID.fromString(resourceId.get()), authorizations, ctx);
    }

    private void checkIfAuthorized(UUID resourceId, Map<String, List<UUID>> authorizations, ContainerRequestContext ctx) {
        List<UUID> authorizedResourceIds = authorizations.getOrDefault(getAuthorizationType().getTokenValue(), Collections.emptyList());
        if (!authorizedResourceIds.contains(resourceId)){
            throw new UnauthorizedException("Not allowed to access resource of type " + getAuthorizationType().name() +"!");
        }
    }

    abstract AuthorizationType getAuthorizationType();
}