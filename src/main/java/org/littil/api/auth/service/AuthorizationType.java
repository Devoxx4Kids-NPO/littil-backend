package org.littil.api.auth.service;

import javax.json.JsonString;
import java.util.*;
import java.util.stream.Stream;

public enum AuthorizationType {
    SCHOOL("schools"),
    GUEST_TEACHER("guest_teachers"),
    USER("users"),
    UNKNOWN("unkown");

    final String tokenValue;

    AuthorizationType(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public boolean hasAny(Map<String, List<JsonString>> authorizations)  {
        return authorizations(authorizations)
                .anyMatch(Objects::nonNull);
    }

    public Stream<UUID> authorizationIds(Map<String, List<String>> authorizations) {
        return authorizations(authorizations)
                .map(UUID::fromString);
    }

    private <T> Stream<T> authorizations(Map<String, List<T>> authorizations) {
        return authorizations.getOrDefault(getTokenValue(), Collections.emptyList())
                .stream();
    }
}
