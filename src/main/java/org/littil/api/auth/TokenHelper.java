package org.littil.api.auth;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.littil.api.auth.service.AuthorizationType;
import org.littil.api.exception.AuthenticationException;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.util.*;

import static org.littil.api.Util.AUTHORIZATIONS_TOKEN_CLAIM;
import static org.littil.api.Util.USER_ID_TOKEN_CLAIM;

@RequestScoped
public class TokenHelper {
    @Inject
    JsonWebToken jwt;
    @ConfigProperty(name = "org.littil.auth.namespace")
    String nameSpace;

    public <T> T getCustomClaim(String claimName) {
        return jwt.getClaim(nameSpace + claimName);
    }

    public UUID getCurrentUserId() {
        String userId = getCustomClaim(USER_ID_TOKEN_CLAIM);
        return Optional.ofNullable(userId)
                .map(UUID::fromString)
                .orElseThrow(() -> {
                    throw new AuthenticationException(String.format("Unable to retrieve LiTTiL userId from JWT token with provider id %s", jwt.getIssuer()));
                });
    }

    public String getSubject() {
        return jwt.getSubject();
    }

    public Boolean hasUserAuthorizations() {
        final Map<String, List<UUID>> authorizations = getUserAuthorizations();
        return authorizations != null &&
                !authorizations.isEmpty() &&
                (!hasSchoolAuthorizations(authorizations) || !hasGuestTeacherAuthorizations(authorizations));
    }

    private Map<String, List<UUID>> getUserAuthorizations() {
        return getCustomClaim(AUTHORIZATIONS_TOKEN_CLAIM);
    }

    private Boolean hasSchoolAuthorizations(Map<String, List<UUID>> authorizations) {
        return authorizations.getOrDefault(AuthorizationType.SCHOOL.getTokenValue(), new ArrayList<>()).size() > 0;
    }

    private Boolean hasGuestTeacherAuthorizations(Map<String, List<UUID>> authorizations) {
        return authorizations.getOrDefault(AuthorizationType.GUEST_TEACHER.getTokenValue(), new ArrayList<>()).size() > 0;

    }
}
