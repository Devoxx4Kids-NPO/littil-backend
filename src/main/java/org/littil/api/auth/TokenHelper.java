package org.littil.api.auth;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.littil.api.auth.service.AuthorizationType;
import org.littil.api.exception.AuthenticationException;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.littil.api.Util.AUTHORIZATIONS_TOKEN_CLAIM;
import static org.littil.api.Util.USER_ID_TOKEN_CLAIM;

@RequestScoped
public class TokenHelper {
    @Inject
    JsonWebToken jwt;

    public UUID getCurrentUserId() {
        String userId = jwt.getClaim(USER_ID_TOKEN_CLAIM);
        return Optional.ofNullable(userId)
                .map(UUID::fromString)
                .orElseThrow(() -> {
                    throw new AuthenticationException(String.format("Unable to retrieve LiTTiL userId from JWT token with provider id %s", jwt.getIssuer()));
                });
    }

    public String getSubject() {
        return jwt.getSubject();
    }

    public Map<String, Set<UUID>> getUserAuthorizations() {
        return jwt.getClaim(AUTHORIZATIONS_TOKEN_CLAIM);
    }

    public Boolean hasUserAuthorizations() {
        final Map<String, Set<UUID>> authorizations = getUserAuthorizations();
        return authorizations != null && !authorizations.isEmpty();
    }

    public Boolean hasSchoolAuthorizations() {
        return getUserAuthorizations().containsKey(AuthorizationType.SCHOOL.getTokenValue());
    }

    public Boolean hasGuestTeacherAuthorizations() {
        return getUserAuthorizations().containsKey(AuthorizationType.GUEST_TEACHER.getTokenValue());
    }
}
