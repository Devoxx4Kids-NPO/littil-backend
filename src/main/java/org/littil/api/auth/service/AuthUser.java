package org.littil.api.auth.service;

import lombok.Data;
import org.littil.api.auth.provider.Provider;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
public class AuthUser {
    private String providerId;
    private String emailAddress;
    private Provider provider;
    private Set<String> roles = new HashSet<>();
    private Map<String, Object> appMetadata;
    private Integer loginsCount;
    private Date lastLogin;
}
