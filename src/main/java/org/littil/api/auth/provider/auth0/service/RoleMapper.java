package org.littil.api.auth.provider.auth0.service;

import org.littil.api.auth.Role;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
//TODO implement MapStruct
public class RoleMapper {

    com.auth0.json.mgmt.Role toDomain(Role role) {
        com.auth0.json.mgmt.Role mgmtRole = new com.auth0.json.mgmt.Role();
        mgmtRole.setName(role.role());
        return mgmtRole;
    }

    Role toEntity(com.auth0.json.mgmt.Role role) {
        return new Role(role.getId(), role.getName());
    }
}
