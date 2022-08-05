package org.littil.api.auth.provider.auth0.service;

import com.auth0.json.mgmt.users.User;
import org.mapstruct.Mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
//@Mapper(componentModel = "cdi")
public class Auth0UserMapper {

	@Inject
	RoleMapper roleMapper;

	User toProviderEntity(org.littil.api.user.service.User littleUser) {
		User auth0User = new User("Username-Password-Authentication");
		auth0User.setEmail(littleUser.getEmailAddress());
		auth0User.setPassword(UUID.randomUUID().toString());
		return auth0User;
	}

	public org.littil.api.user.service.User toDomain(User user) {
		org.littil.api.user.service.User user1 = new org.littil.api.user.service.User();
		user1.setEmailAddress(user.getEmail());
		user1.setId(user.getId());
		// todo check how to retrieve roles from auth0 user
		user1.setRoles( ((Set<com.auth0.json.mgmt.Role>) user.getValues().get("roles"))
				.stream()
				.map(roleMapper::toEntity)
				.collect(Collectors.toSet()));
		// todo set teacher/school -> retrieve from db?
		user1.setGuestTeacher(null);
		user1.setSchool(null);
		return user1;
	}
}
