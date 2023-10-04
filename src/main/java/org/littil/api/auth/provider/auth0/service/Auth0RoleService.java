package org.littil.api.auth.provider.auth0.service;

import com.auth0.json.mgmt.roles.Role;
import org.littil.api.auth.provider.auth0.exception.Auth0RoleException;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class Auth0RoleService {
    private final Map<String, String> roleIdMapping = new HashMap<>();

    @Inject
    Auth0AuthenticationService auth0;


    /**
     * If roleName not present in roleIdMapping map, retrieve from auth0 and store (cache) it
     * @param roleName roleName to retrieve id for
     * @return Id, if not found throws Auth0RoleException
     */
    String getIdForRoleName(String roleName){
        if (roleIdMapping.containsKey(roleName)) {
            return roleIdMapping.get(roleName);
        }
        Optional<Role> roleOptional = this.auth0.getRoleByName(roleName);
        if (roleOptional.isPresent()){
            String roleId = roleOptional.get().getId();
            roleIdMapping.put(roleName, roleId);
            return roleId;
        }
        throw new Auth0RoleException("Could not find role " + roleName);
    }
}
