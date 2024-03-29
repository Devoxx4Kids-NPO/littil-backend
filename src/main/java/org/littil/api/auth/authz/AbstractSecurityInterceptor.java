package org.littil.api.auth.authz;

import io.quarkus.security.UnauthorizedException;
import jakarta.annotation.Priority;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.littil.api.auth.TokenHelper;
import org.littil.api.auth.service.AuthorizationType;

import jakarta.inject.Inject;
import jakarta.json.JsonString;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Priority(Priorities.AUTHORIZATION)
//@Provider
@Slf4j
@NoArgsConstructor
public abstract class AbstractSecurityInterceptor implements ContainerRequestFilter {
    @Inject
    TokenHelper tokenHelper;

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        if (ctx.getMethod().equals("GET") || ctx.getMethod().equals("PUT")) {
            // GET methods are public, so always allowed
            // PUT methods are used to create or update an instance.
            // For update actions the service class checks if the user
            // is the owner of the instance.
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
        Map<String, List<JsonString>> authorizations = tokenHelper.getAuthorizations();

        checkIfAuthorized(UUID.fromString(resourceId.get()), authorizations);
    }

    private void checkIfAuthorized(UUID resourceId, Map<String, List<JsonString>> authorizations) {
        if (authorizations == null) {
            throw new UnauthorizedException("Not allowed to access resource of type " + getAuthorizationType().name() + "!");
        }
        List<JsonString> authorizedResourceIds = authorizations.getOrDefault(getAuthorizationType().getTokenValue(), Collections.emptyList());
        if (authorizedResourceIds.stream().noneMatch(id -> id.getChars().equals(resourceId.toString()))) {
            throw new UnauthorizedException("Not allowed to access resource of type " + getAuthorizationType().name() + "!");
        }
    }

    abstract AuthorizationType getAuthorizationType();
}
