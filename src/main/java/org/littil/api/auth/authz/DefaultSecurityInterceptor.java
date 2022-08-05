package org.littil.api.auth.authz;

import io.quarkus.arc.Priority;
import io.quarkus.security.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.littil.api.user.service.User;
import org.littil.api.user.service.UserService;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.littil.api.Util.USER_ID_TOKEN_CLAIM;
import static org.littil.api.auth.authz.UserOwned.SecurityType.DEFAULT;

@Priority(3000)
@Provider
@Slf4j
@UserOwned(type = DEFAULT)
public class DefaultSecurityInterceptor implements ContainerRequestFilter {
    @Inject
    JsonWebToken jwt;

    @Inject
    UserService userService;

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {

        MultivaluedMap<String, String> parameters = ctx.getUriInfo().getPathParameters();
        List<String> resourceIds = parameters.get("id");
        if (resourceIds.size() != 1) {
            throw new IllegalArgumentException("Whoops we dit not expect this amount of parameters");
        }
        Optional<String> resourceId = resourceIds.stream().findFirst();
        UUID userId = UUID.fromString(jwt.getClaim(USER_ID_TOKEN_CLAIM));

        User user = userService.getUserById(userId);
        checkIfAuthorized(UUID.fromString(resourceId.get()), userId, user, ctx);

        if (jwt.getClaim("user_id"))

        //if !( jwt.getUserID ()  user check in token voor dit request) {
            ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }

    private void checkIfAuthorized(UUID resourceId, UUID userId, User user, ContainerRequestContext ctx) {
        if (user.getSchool() != null && !user.getSchool().getId().equals(resourceId)) {
            // todo use own custom exception
            throw new UnauthorizedException("Not allowed to access this school!");
        } else if (user.getGuestTeacher() != null && !user.getGuestTeacher().getId().equals(resourceId)) {
            // todo use own custom exception
            throw new UnauthorizedException("Not allowed to access this guest teacher!");
        } else {
            log.warn("Do not know what to do for user {} with resource id {} for url {}", userId, resourceId, ctx.getUriInfo().getRequestUri());
            throw new IllegalArgumentException("Unknown what to do using given parameters");
        }
    }
}
