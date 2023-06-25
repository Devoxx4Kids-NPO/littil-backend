package org.littil.api.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.littil.api.auth.service.AuthorizationType;
import org.littil.api.exception.AuthenticationException;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonString;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequestScoped
public class TokenHelper {

    public static ObjectMapper objectMapper = new ObjectMapper();

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

    public Map<String, List<JsonString>> getAuthorizations() {
        Map<String, List<JsonString>> authorizations = getCustomClaim(authorizationsClaimName);
        return Optional.ofNullable(authorizations)
                .orElse(Collections.emptyMap());
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
        final Map<String, List<JsonString>> authorizations = getAuthorizations();
        return hasSchoolAuthorizations(authorizations) || hasGuestTeacherAuthorizations(authorizations);
    }

    private static boolean hasSchoolAuthorizations(Map<String, List<JsonString>> authorizations) {
        return AuthorizationType.SCHOOL.hasAny(authorizations);
    }

    private static boolean hasGuestTeacherAuthorizations(Map<String, List<JsonString>> authorizations) {
        return AuthorizationType.GUEST_TEACHER.hasAny(authorizations);
    }
}
