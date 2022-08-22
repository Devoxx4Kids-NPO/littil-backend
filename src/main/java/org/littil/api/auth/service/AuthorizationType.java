package org.littil.api.auth.service;

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
}
