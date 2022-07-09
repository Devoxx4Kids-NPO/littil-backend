package org.littil.api.auth.auth0;

import javax.enterprise.context.ApplicationScoped;

import org.littil.api.auth.RoleEntity;

import com.auth0.json.mgmt.Role;

@ApplicationScoped
public class RoleMapper {

	Role toDomain(RoleEntity roleEntity) {
		Role role = new Role();
		role.setName(roleEntity.getRole());
		return role;
	};

	RoleEntity toEntity(Role role) {
		return new RoleEntity(role.getId(), role.getName());
	}
}
