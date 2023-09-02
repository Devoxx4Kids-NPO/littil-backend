package org.littil.api.auth.provider.auth0.service;

import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.filter.RolesFilter;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.mgmt.roles.Role;
import com.auth0.json.mgmt.roles.RolesPage;
import com.auth0.net.Response;
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
    ManagementAPI managementAPI;


    /**
     * If roleName not present in roleIdMapping map, retrieve from auth0 and store (cache) it
     * @param roleName roleName to retrieve id for
     * @return Id, if not found throws Auth0RoleException
     */
    String getIdForRoleName(String roleName){
        if (roleIdMapping.containsKey(roleName)) {
            return roleIdMapping.get(roleName);
        }

        RolesPage roles = null;
        try {
            Response<RolesPage> response = managementAPI.roles().list(new RolesFilter().withName(roleName)).execute();
            roles = response.getBody();
        } catch (Auth0Exception e) {
            throw new Auth0RoleException("Could not retrieve role for " + roleName, e);
        }
        Optional<Role> roleOptional = roles.getItems().stream().findFirst();
        if (roleOptional.isPresent()){
            String roleId = roleOptional.get().getId();
            roleIdMapping.put(roleName, roleId);
            return roleId;
        }
        throw new Auth0RoleException("Could not find role " + roleName);
    }
}
