package org.littil.api.auth.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public enum AuthorizationType {
    SCHOOL("schools"),
    GUEST_TEACHER("guest_teachers"),
    USER("users"),
    UNKNOWN("unkown");

    final String tokenValue;

    private AuthorizationType(String tokenvalue) {
        this.tokenValue = tokenvalue;
    }

    public String getTokenValue(){
        return tokenValue;
    }

    public Stream<UUID> authorizationIds(Map<String, List<String>> authorizations) {
        return authorizations.getOrDefault(getTokenValue(), Collections.emptyList())
                .stream()
                .map(UUID::fromString);
    }
}
