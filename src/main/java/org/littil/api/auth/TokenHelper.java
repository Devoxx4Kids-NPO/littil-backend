package org.littil.api.auth;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.littil.api.auth.service.AuthorizationType;
import org.littil.api.exception.AuthenticationException;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequestScoped
public class TokenHelper {

    @Inject
    JsonWebToken accessToken;

    @Inject
    @ConfigProperty(name = "org.littil.auth.token.claim.namespace")
    String claimNamespace;

    @Inject
    @ConfigProperty(name = "org.littil.auth.token.claim.user_id")
    String userIdClaimName;

    @Inject
    @ConfigProperty(name = "org.littil.auth.token.claim.authorizations")
    String authorizationsClaimName;

    public <T> T getCustomClaim(String claimName) {
        return accessToken.getClaim(claimNamespace.concat(claimName));
    }

    public Optional<UUID> currentUserId() {
        String userId = getCustomClaim(userIdClaimName);
        return Optional.ofNullable(userId)
                .map(UUID::fromString);
    }

    public UUID getCurrentUserId() {
        return currentUserId()
                .orElseThrow(() -> {
                   throw new AuthenticationException(String.format("Unable to retrieve LiTTiL userId from JWT token with provider id %s", accessToken.getIssuer()));
                });
    }

    public Boolean hasUserAuthorizations() {
        final Map<String, List<String>> authorizations = Optional.ofNullable(getUserAuthorizations()).orElse(Collections.emptyMap());
        return hasSchoolAuthorizations(authorizations) || hasGuestTeacherAuthorizations(authorizations);
    }

    private Map<String, List<String>> getUserAuthorizations() {
        return getCustomClaim(authorizationsClaimName);
    }

    private static boolean hasSchoolAuthorizations(Map<String, List<String>> authorizations) {
        return AuthorizationType.SCHOOL.authorizationIds(authorizations).findAny().isPresent();
    }

    private static boolean hasGuestTeacherAuthorizations(Map<String, List<String>> authorizations) {
        return AuthorizationType.GUEST_TEACHER.authorizationIds(authorizations).findAny().isPresent();
    }
}
