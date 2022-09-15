package org.littil.api.auth.authz;

import io.quarkus.arc.Priority;
import io.quarkus.security.UnauthorizedException;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.littil.api.auth.TokenHelper;
import org.littil.api.auth.service.AuthorizationType;

import javax.inject.Inject;
import javax.json.JsonString;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
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

    @Inject
    @ConfigProperty(name = "org.littil.auth.token.claim.authorizations")
    String authorizationsClaimName;

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        if (ctx.getMethod().equals("GET") || ctx.getMethod().equals("POST") || ctx.getMethod().equals("PUT")) {
            // GET methods are public, so always allowed
            // POST methods are used to create new instances that attached to the current user. Allowed for now.
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
        Map<String, List<JsonString>> authorizations = tokenHelper.getCustomClaim(authorizationsClaimName);

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
