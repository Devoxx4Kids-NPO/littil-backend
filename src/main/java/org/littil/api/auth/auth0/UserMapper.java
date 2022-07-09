package org.littil.api.auth.auth0;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import org.littil.api.auth.provider.Provider;

import com.auth0.json.mgmt.users.User;

@ApplicationScoped
public class UserMapper {

	User toDomain(org.littil.api.auth.User littleUser) {
		User auth0User = new User("Username-Password-Authentication");
		auth0User.setEmail(littleUser.getEmailAddress());
		auth0User.setPassword(UUID.randomUUID().toString());
		return auth0User;
	};

	org.littil.api.auth.User toEntity(User auth0User) {
		return new org.littil.api.auth.User(auth0User.getId(), auth0User.getEmail(), null, Provider.AUTH0, null, null); // TODO
	}

}
