package org.littil.api.auth.service;

import lombok.Data;
import org.littil.api.auth.Role;
import org.littil.api.auth.provider.Provider;

import java.util.HashSet;
import java.util.Set;

@Data
public class AuthUser {
    private String id;
    private String emailAddress;
    private Provider authProvider;
    private Set<Role> roles = new HashSet<>();
}
