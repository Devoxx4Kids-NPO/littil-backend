package org.littil.api.auth.provider.auth0.service;

import com.auth0.json.mgmt.roles.Role;

import jakarta.inject.Singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class Auth0RoleService {
    private final Map<String, String> roleIdMapping = new ConcurrentHashMap<>();
    private final Auth0AuthenticationService auth0;

    public Auth0RoleService(Auth0AuthenticationService auth0) {
        this.auth0 = auth0;
    }

    /**
     * If roleName not present in roleIdMapping map, retrieve from auth0 and store (cache) it
     * @param roleName roleName to retrieve id for
     * @return Id, if not found throws Auth0RoleException
     */
    String getIdForRoleName(String roleName){
        return this.roleIdMapping.computeIfAbsent(roleName,this::lookupId);
    }

    private String lookupId(String roleName) {
        return this.auth0.getRoleByName(roleName)
                .map(Role::getId)
                .orElse(null);
    }
}
